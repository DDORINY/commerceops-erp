# CommerceOps AI inference API

FastAPI service for baseline checkpoints and an image-upload quality endpoint.

GET /health reports checkpoint availability. POST /predict accepts task/features. POST /predict/image accepts JPEG, PNG, WebP or GIF up to 5 MiB. The image endpoint is a safe baseline quality heuristic, not a trained vision model.

Production compose mounts ai/models/checkpoints read-only at /app/models; checkpoints are provisioned separately because they are excluded from Git.
