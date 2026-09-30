#!/usr/bin/env bash
# Sobe a API em modo dev (H2 em memoria + usuario admin de seed).
# Uso: ./dev.sh [porta]
set -e

PORT="${1:-8080}"

cd "$(dirname "$0")"
./mvnw spring-boot:run \
  -Dspring-boot.run.profiles=dev \
  -Dspring-boot.run.arguments=--server.port="$PORT"
