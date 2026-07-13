"""CommerceOps ERP AI baseline evaluation script."""

from __future__ import annotations

import argparse
import csv
import json
import math
from datetime import datetime, timezone
from pathlib import Path
from typing import Any


def load_json(path: Path) -> Any:
    with path.open("r", encoding="utf-8-sig") as file:
        return json.load(file)


def load_rows(dataset_path: Path, dataset_format: str) -> list[dict[str, Any]]:
    if not dataset_path.exists():
        raise FileNotFoundError(f"검증 데이터셋 파일을 찾을 수 없습니다: {dataset_path}")

    if dataset_format == "json":
        payload = load_json(dataset_path)
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


def numeric(value: Any) -> float:
    if isinstance(value, bool):
        return 1.0 if value else 0.0
    if isinstance(value, (int, float)):
        return float(value)
    try:
        return float(value)
    except (TypeError, ValueError):
        return 0.0


def infer_feature_columns(rows: list[dict[str, Any]], target_column: str) -> list[str]:
    if not rows:
        return []
    return [
        key for key, value in rows[0].items()
        if key != target_column and isinstance(value, (int, float, bool))
    ]


def build_matrix(
    rows: list[dict[str, Any]],
    feature_columns: list[str],
    target_column: str,
) -> tuple[list[list[float]], list[float]]:
    return (
        [[numeric(row.get(column)) for column in feature_columns] for row in rows],
        [numeric(row.get(target_column)) for row in rows],
    )


def evaluate_checkpoint(checkpoint_path: Path, x_rows: list[list[float]], y_rows: list[float]) -> dict[str, Any]:
    if not checkpoint_path.exists():
        return {"checkpointEvaluated": False, "reason": "checkpoint 파일이 없습니다."}

    try:
        import torch
    except ImportError:
        return {"checkpointEvaluated": False, "reason": "PyTorch가 설치되어 있지 않습니다."}

    if not x_rows or not y_rows:
        return {"checkpointEvaluated": False, "reason": "평가할 row가 없습니다."}

    checkpoint = torch.load(checkpoint_path, map_location="cpu")
    feature_count = len(x_rows[0])
    model = torch.nn.Linear(feature_count, 1)
    model.load_state_dict(checkpoint["model_state_dict"])
    model.eval()

    with torch.no_grad():
        x_tensor = torch.tensor(x_rows, dtype=torch.float32)
        y_tensor = torch.tensor(y_rows, dtype=torch.float32).view(-1, 1)
        predictions = model(x_tensor)
        errors = predictions - y_tensor
        mae = torch.mean(torch.abs(errors)).item()
        rmse = math.sqrt(torch.mean(errors * errors).item())

    return {
        "checkpointEvaluated": True,
        "mae": mae,
        "rmse": rmse,
    }


def write_reports(report_dir: Path, report_name: str, report: dict[str, Any]) -> tuple[Path, Path]:
    report_dir.mkdir(parents=True, exist_ok=True)
    timestamp = datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ")
    base = f"{report_name}_{timestamp}"
    json_path = report_dir / f"{base}.json"
    md_path = report_dir / f"{base}.md"

    with json_path.open("w", encoding="utf-8") as file:
        json.dump(report, file, ensure_ascii=False, indent=2)

    lines = [
        "# AI 모델 평가 리포트",
        "",
        f"- 생성 시각: {report['evaluatedAt']}",
        f"- 데이터셋: `{report['datasetPath']}`",
        f"- row 수: {report['rowCount']}",
        f"- feature columns: {', '.join(report['featureColumns']) if report['featureColumns'] else '(없음)'}",
        f"- target column: `{report['targetColumn']}`",
        f"- checkpoint 평가: {'성공' if report['metrics'].get('checkpointEvaluated') else '미실행'}",
    ]
    if report["metrics"].get("checkpointEvaluated"):
        lines.extend([
            f"- MAE: {report['metrics']['mae']:.6f}",
            f"- RMSE: {report['metrics']['rmse']:.6f}",
        ])
    else:
        lines.append(f"- 사유: {report['metrics'].get('reason', '알 수 없음')}")
    lines.extend([
        "",
        "개인정보 원문은 리포트에 포함하지 않는다.",
        "",
    ])
    md_path.write_text("\n".join(lines), encoding="utf-8")
    return json_path, md_path


def main() -> None:
    parser = argparse.ArgumentParser(description="CommerceOps ERP AI baseline evaluator")
    parser.add_argument("--config", required=True, help="평가 설정 JSON 경로")
    args = parser.parse_args()

    config = load_json(Path(args.config))
    dataset_path = Path(config["dataset_path"])
    dataset_format = str(config.get("dataset_format", "json"))
    target_column = str(config.get("target_column", "target"))
    rows = load_rows(dataset_path, dataset_format)
    feature_columns = list(config.get("feature_columns") or infer_feature_columns(rows, target_column))
    x_rows, y_rows = build_matrix(rows, feature_columns, target_column)

    checkpoint_path = Path(config.get("checkpoint_path", ""))
    metrics = evaluate_checkpoint(checkpoint_path, x_rows, y_rows)

    metadata_path = Path(config.get("metadata_path", ""))
    training_metadata = load_json(metadata_path) if metadata_path.exists() else {}
    report = {
        "evaluatedAt": datetime.now(timezone.utc).isoformat(),
        "datasetPath": str(dataset_path),
        "datasetFormat": dataset_format,
        "checkpointPath": str(checkpoint_path),
        "metadataPath": str(metadata_path),
        "rowCount": len(rows),
        "featureColumns": feature_columns,
        "targetColumn": target_column,
        "trainingMetadata": training_metadata,
        "metrics": metrics,
        "privacyNote": "평가 입력 데이터와 리포트에는 개인정보 원문을 포함하지 않습니다.",
    }

    json_path, md_path = write_reports(
        Path(config.get("report_dir", "ai/reports/generated")),
        str(config.get("report_name", "baseline_evaluation")),
        report,
    )
    print(f"평가 rows: {len(rows)}")
    print(f"JSON 리포트 저장: {json_path}")
    print(f"Markdown 리포트 저장: {md_path}")


if __name__ == "__main__":
    main()
