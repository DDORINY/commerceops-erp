#!/bin/sh

set -eu

project_dir=/home/ubuntu/commerceops-erp

cd "$project_dir"

docker compose \
    --env-file .env.prod \
    -f docker-compose.prod.yml \
    --profile tls \
    run --rm --no-deps certbot \
    renew --webroot --webroot-path /var/www/certbot --quiet "$@"

docker compose \
    --env-file .env.prod \
    -f docker-compose.prod.yml \
    exec -T nginx nginx -s reload
