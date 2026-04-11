#!/usr/bin/env bash
set -e

cd "$(dirname "$0")"

docker compose --env-file .env -f docker/compose.yaml --project-directory docker exec tomcat ant deploy_tomcat
