#!/usr/bin/env bash

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
COMPOSE_FILE="${ROOT}/compose.yaml"
CMD="${1:-up}"

info() {
  printf '[dev] %s\n' "$*"
}

require_command() {
  command -v "$1" >/dev/null 2>&1 || {
    printf '[dev] missing required command: %s\n' "$1" >&2
    exit 1
  }
}

require_command docker

case "${CMD}" in
  up)
    info "Starting app stack with observability"
    docker compose -f "${COMPOSE_FILE}" up --build -d
    info "Frontend: http://localhost:5173"
    info "Grafana: http://localhost:3000"
    info "Prometheus: http://localhost:9090"
    info "Platform: http://localhost:8082"
    info "Backend:  http://localhost:8081"
    info "Origination: http://localhost:8083"
    info "Customer: http://localhost:8084"
    info "Pawn ticket: http://localhost:8085"
    info "Reporting: http://localhost:8091"
    info "AML: http://localhost:8088"
    ;;
  down)
    info "Stopping local environment"
    docker compose -f "${COMPOSE_FILE}" down
    ;;
  reset)
    info "Stopping local environment and deleting volumes"
    docker compose -f "${COMPOSE_FILE}" down -v
    ;;
  logs)
    docker compose -f "${COMPOSE_FILE}" logs -f
    ;;
  ps)
    docker compose -f "${COMPOSE_FILE}" ps
    ;;
  build)
    info "Building local images"
    docker compose -f "${COMPOSE_FILE}" build
    ;;
  restart)
    info "Restarting local environment"
    docker compose -f "${COMPOSE_FILE}" down
    docker compose -f "${COMPOSE_FILE}" up --build -d
    ;;
  *)
    cat <<'EOF'
Usage: ./infra/scripts/dev.sh [up|down|reset|logs|ps|build|restart]

  up       Build and start the local stack including Prometheus and Grafana
  down     Stop all local containers
  reset    Stop containers and remove named volumes
  logs     Tail compose logs
  ps       Show service status
  build    Build the service images without starting them
  restart  Rebuild and restart all services
EOF
    exit 1
    ;;
esac
