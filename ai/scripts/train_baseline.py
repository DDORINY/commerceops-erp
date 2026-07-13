"""CommerceOps ERP AI baseline training script.

포트폴리오 데모용 합성 데이터나 관리자 export 데이터로 간단한 baseline 모델을 학습한다.
PyTorch가 설치된 환경에서는 `.pt` 체크포인트를 저장하고, PyTorch가 없으면 메타데이터만 저장한다.
"""

from __future__ import annotations

import argparse
import csv
import json
import random
from datetime import datetime, timezone
from pathlib import Path
from typing import Any


def load_config(path: Path) -> dict[str, Any]:
    with path.open("r", encoding="utf-8-sig") as file:
        return json.load(file)


def load_rows(dataset_path: Path, dataset_format: str) -> list[dict[str, Any]]:
    if not dataset_path.exists():
        raise FileNotFoundError(f"데이터셋 파일을 찾을 수 없습니다: {dataset_path}")

    if dataset_format == "json":
        with dataset_path.open("r", encoding="utf-8-sig") as file:
            payload = json.load(file)
        rows = payload.get("rows", []) if isinstance(payload, dict) else payload
        return [row for row in rows if isinstance(row, dict)]

    if dataset_format == "jsonl":
        rows: list[dict[str, Any]] = []
        with dataset_path.open("r", encoding="utf-8-sig") as file:
            for line in file:
                line = line.strip()
                if line:
                    row = json.loads(line)
                    if isinstance(row, dict):
                        rows.append(row)
        return rows

    if dataset_format == "csv":
        with dataset_path.open("r", encoding="utf-8-sig", newline="") as file:
            return list(csv.DictReader(file))

    raise ValueError(f"지원하지 않는 dataset_format입니다: {dataset_format}")


def infer_feature_columns(rows: list[dict[str, Any]], target_column: str) -> list[str]:
    if not rows:
        return []
    return [
        key for key, value in rows[0].items()
        if key != target_column and isinstance(value, (int, float, bool))
    ]


def numeric(value: Any) -> float:
    if isinstance(value, bool):
        return 1.0 if value else 0.0
    if isinstance(value, (int, float)):
        return float(value)
    try:
        return float(value)
    except (TypeError, ValueError):
        return 0.0


def build_matrix(
    rows: list[dict[str, Any]],
    feature_columns: list[str],
    target_column: str,
) -> tuple[list[list[float]], list[float]]:
    x_rows = [[numeric(row.get(column)) for column in feature_columns] for row in rows]
    y_rows = [numeric(row.get(target_column)) for row in rows]
    return x_rows, y_rows


def artifact_paths(output_dir: Path, model_name: str, artifact_stem: str | None) -> tuple[Path, Path]:
    output_dir.mkdir(parents=True, exist_ok=True)
    if artifact_stem:
        stem = artifact_stem
    else:
        timestamp = datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ")
        stem = f"{model_name}_{timestamp}"
    return output_dir / f"{stem}.pt", output_dir / f"{stem}.metadata.json"


def save_metadata(metadata_path: Path, metadata: dict[str, Any]) -> Path:
    metadata_path.parent.mkdir(parents=True, exist_ok=True)
    with metadata_path.open("w", encoding="utf-8") as file:
        json.dump(metadata, file, ensure_ascii=False, indent=2)
    return metadata_path


def save_torch_checkpoint(
    checkpoint_path: Path,
    x_rows: list[list[float]],
    y_rows: list[float],
    metadata: dict[str, Any],
) -> Path | None:
    try:
        import torch
    except ImportError:
        return None

    checkpoint_path.parent.mkdir(parents=True, exist_ok=True)
    feature_count = len(x_rows[0]) if x_rows else 1
    model = torch.nn.Linear(feature_count, 1)

    if x_rows and y_rows:
        torch.manual_seed(int(metadata["randomSeed"]))
        x_tensor = torch.tensor(x_rows, dtype=torch.float32)
        y_tensor = torch.tensor(y_rows, dtype=torch.float32).view(-1, 1)
        optimizer = torch.optim.SGD(model.parameters(), lr=0.001)
        loss_fn = torch.nn.MSELoss()
        for _ in range(30):
            optimizer.zero_grad()
            loss = loss_fn(model(x_tensor), y_tensor)
            loss.backward()
            optimizer.step()
        metadata["trainingLoss"] = float(loss.detach().item())

    torch.save({"model_state_dict": model.state_dict(), "metadata": metadata}, checkpoint_path)
    return checkpoint_path


def main() -> None:
    parser = argparse.ArgumentParser(description="CommerceOps ERP AI baseline trainer")
    parser.add_argument("--config", required=True, help="학습 설정 JSON 경로")
    args = parser.parse_args()

    config_path = Path(args.config)
    config = load_config(config_path)
    random.seed(int(config.get("random_seed", 42)))

    dataset_path = Path(config["dataset_path"])
    rows = load_rows(dataset_path, str(config.get("dataset_format", "json")))[: int(config.get("max_rows", 5000))]
    target_column = str(config.get("target_column", "target"))
    feature_columns = list(config.get("feature_columns") or infer_feature_columns(rows, target_column))
    x_rows, y_rows = build_matrix(rows, feature_columns, target_column)

    output_dir = Path(config.get("output_dir", "ai/models/checkpoints"))
    model_name = str(config.get("model_name", "commerceops_baseline"))
    checkpoint_path, metadata_path = artifact_paths(output_dir, model_name, config.get("artifact_stem"))

    metadata: dict[str, Any] = {
        "modelName": model_name,
        "taskType": config.get("task_type", "regression"),
        "datasetPath": str(dataset_path),
        "datasetFormat": config.get("dataset_format", "json"),
        "rowCount": len(rows),
        "featureColumns": feature_columns,
        "targetColumn": target_column,
        "randomSeed": int(config.get("random_seed", 42)),
        "createdAt": datetime.now(timezone.utc).isoformat(),
        "privacyNote": "학습 입력 파일은 개인정보가 없는 합성 데이터이거나 export 단계에서 마스킹된 데이터여야 합니다.",
    }

    saved_checkpoint = save_torch_checkpoint(checkpoint_path, x_rows, y_rows, metadata)
    save_metadata(metadata_path, metadata)

    print(f"학습 rows: {len(rows)}")
    print(f"feature columns: {', '.join(feature_columns) if feature_columns else '(없음)'}")
    if saved_checkpoint:
        print(f"체크포인트 저장: {saved_checkpoint}")
    else:
        print("PyTorch가 없어 .pt 체크포인트 생성은 건너뛰고 메타데이터만 저장했습니다.")
    print(f"메타데이터 저장: {metadata_path}")


if __name__ == "__main__":
    main()
