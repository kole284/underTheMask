#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

if [ ! -f .env ]; then
  echo "Missing .env. Copy .env.production.example to .env and fill production values." >&2
  exit 1
fi

docker compose pull db
docker compose build
docker compose up -d
docker compose ps
