from __future__ import annotations

import io
import os
from pathlib import Path
from typing import Any, Literal

import torch
from fastapi import FastAPI, File, HTTPException, UploadFile
from PIL import Image, UnidentifiedImageError
from pydantic import BaseModel, Field


MODEL_DIR = Path(os.getenv("AI_MODEL_DIR", "/app/models"))
MAX_IMAGE_BYTES = int(os.getenv("AI_MAX_IMAGE_BYTES", str(5 * 1024 * 1024)))


class PredictionRequest(BaseModel):
    task: Literal["demand", "review"]
    features: dict[str, float | int | bool] = Field(default_factory=dict)


class PredictionResponse(BaseModel):
    task: str
    modelName: str
    prediction: float
    features: dict[str, float]


class ModelRegistry:
    specs = {
        "demand": ("commerceops_demo_demand_baseline", "commerceops_demo_demand_baseline.pt"),
        "review": ("commerceops_demo_review_sentiment_baseline", "commerceops_demo_review_sentiment_baseline.pt"),
    }

    def __init__(self) -> None:
        self.models: dict[str, tuple[torch.nn.Module, list[str], str]] = {}
        for task, (name, filename) in self.specs.items():
            path = MODEL_DIR / filename
            metadata_path = path.with_suffix(".metadata.json")
            if not path.exists() or not metadata_path.exists():
                continue
            metadata = __import__("json").loads(metadata_path.read_text(encoding="utf-8"))
            columns = list(metadata.get("featureColumns", []))
            checkpoint = torch.load(path, map_location="cpu", weights_only=True)
            model = torch.nn.Linear(len(columns) or 1, 1)
            model.load_state_dict(checkpoint["model_state_dict"])
            model.eval()
            self.models[task] = (model, columns, name)

    def status(self) -> dict[str, Any]:
        return {
            task: {"available": task in self.models, "modelName": name, "features": columns}
            for task, (name, _) in self.specs.items()
            for columns in [self.models[task][1] if task in self.models else []]
        }

    def predict(self, request: PredictionRequest) -> PredictionResponse:
        if request.task not in self.models:
            raise HTTPException(status_code=503, detail=f"{request.task} 모델을 사용할 수 없습니다.")
        model, columns, name = self.models[request.task]
        normalized = {column: self._number(request.features.get(column, 0)) for column in columns}
        tensor = torch.tensor([[normalized[column] for column in columns]], dtype=torch.float32)
        with torch.inference_mode():
            prediction = float(model(tensor).item())
        return PredictionResponse(task=request.task, modelName=name, prediction=prediction, features=normalized)

    @staticmethod
    def _number(value: Any) -> float:
        if isinstance(value, bool):
            return 1.0 if value else 0.0
        try:
            return float(value)
        except (TypeError, ValueError):
            return 0.0


app = FastAPI(title="CommerceOps AI Inference API", version="0.9.8")
registry = ModelRegistry()


@app.get("/health")
def health() -> dict[str, Any]:
    statuses = registry.status()
    return {"status": "UP" if all(item["available"] for item in statuses.values()) else "DEGRADED", "models": statuses}


@app.post("/predict", response_model=PredictionResponse)
def predict(request: PredictionRequest) -> PredictionResponse:
    return registry.predict(request)


@app.post("/predict/image")
async def predict_image(file: UploadFile = File(...)) -> dict[str, Any]:
    if file.content_type not in {"image/jpeg", "image/png", "image/webp", "image/gif"}:
        raise HTTPException(status_code=415, detail="JPEG, PNG, WebP 또는 GIF 이미지만 지원합니다.")
    payload = await file.read(MAX_IMAGE_BYTES + 1)
    if len(payload) > MAX_IMAGE_BYTES:
        raise HTTPException(status_code=413, detail="이미지 크기가 제한을 초과했습니다.")
    try:
        with Image.open(io.BytesIO(payload)) as image:
            image.verify()
        with Image.open(io.BytesIO(payload)) as image:
            rgb = image.convert("RGB")
            brightness = sum(sum(pixel) for pixel in rgb.resize((1, 1)).getdata()) / 3
            quality = "DARK" if brightness < 70 else "BRIGHT" if brightness > 220 else "NORMAL"
            return {"modelName": "image_quality_heuristic", "filename": file.filename, "width": image.width, "height": image.height, "quality": quality, "brightness": round(brightness, 2)}
    except (UnidentifiedImageError, OSError) as exc:
        raise HTTPException(status_code=422, detail="이미지 디코딩에 실패했습니다.") from exc
