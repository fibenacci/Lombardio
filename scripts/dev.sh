#!/usr/bin/env bash

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="${ROOT}/compose.yaml"
DEBUG_FILE="${ROOT}/docker-compose.debug.yml"
CMD="${1:-up}"
shift || true
PROFILES=("$@")

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

compose_args=("-f" "${COMPOSE_FILE}")
compose_env=()

if [ "${#PROFILES[@]}" -eq 0 ]; then
  compose_env=("COMPOSE_PROFILES=${COMPOSE_PROFILES:-ops,auction,obs,aux}")
elif [ "${PROFILES[0]}" = "lean" ]; then
  compose_env=("COMPOSE_PROFILES=")
  PROFILES=()
elif [ "${PROFILES[0]}" = "all" ]; then
  compose_env=("COMPOSE_PROFILES=ops,auction,obs,aux")
else
  joined_profiles="$(IFS=,; printf '%s' "${PROFILES[*]}")"
  compose_env=("COMPOSE_PROFILES=${joined_profiles}")
fi

case "${CMD}" in
  up|debug)
    if [ "${#PROFILES[@]}" -eq 0 ]; then
      info "Starting full local stack"
    elif [ "${compose_env[0]}" = "COMPOSE_PROFILES=" ]; then
      info "Starting lean local stack"
    else
      info "Starting local stack with profiles: ${PROFILES[*]}"
    fi
    
    if [ "${CMD}" = "debug" ]; then
      info "DEBUG MODE ENABLED"
      compose_args+=("-f" "${DEBUG_FILE}")
    fi

    info "Building local stack..."
    DOCKER_BUILDKIT=1 env "${compose_env[@]}" docker compose "${compose_args[@]}" up --build -d
    info "Frontend: http://localhost:5173"
    info "Platform: http://localhost:8082"
    info "Backend:  http://localhost:8081"
    
    active_profiles="${compose_env[0]#COMPOSE_PROFILES=}"
    if printf '%s' "${active_profiles}" | grep -Eq '(^|,)ops(,|$)'; then
      info "Origination: http://localhost:8083"
      info "Identity:    http://localhost:8084"
      info "Pawn ticket: http://localhost:8085"
    fi
    
    if [ "${CMD}" = "debug" ]; then
      info "--- DEBUG PORTS ---"
      info "Platform:              5005"
      info "Loan Origination:      5006"
      info "Identity Intelligence: 5007"
      info "Pawn Ticket:           5008"
      info "Auction:               5009"
      info "Online Auction:        5010"
      info "Reporting:             5011"
    fi

    if printf '%s' "${active_profiles}" | grep -Eq '(^|,)obs(,|$)'; then
      info "Grafana: http://localhost:3000"
      info "Prometheus: http://localhost:9090"
    fi
    ;;
  down)
    info "Stopping local environment"
    env "${compose_env[@]}" docker compose "${compose_args[@]}" down
    ;;
  reset)
    info "Stopping local environment and deleting volumes"
    env "${compose_env[@]}" docker compose "${compose_args[@]}" down -v
    ;;
  logs)
    env "${compose_env[@]}" docker compose "${compose_args[@]}" logs -f
    ;;
  stats)
    docker stats --no-stream
    ;;
  ps)
    env "${compose_env[@]}" docker compose "${compose_args[@]}" ps
    ;;
  build)
    info "Building local images"
    env "${compose_env[@]}" docker compose "${compose_args[@]}" build
    ;;
  restart)
    info "Restarting local environment"
    env "${compose_env[@]}" docker compose "${compose_args[@]}" down
    env "${compose_env[@]}" docker compose "${compose_args[@]}" up --build -d
    ;;
  *)
    cat <<'EOF'
Usage: ./infra/scripts/dev.sh [up|debug|down|reset|logs|stats|ps|build|restart] [profile...]

  profiles  optional: lean, ops, auction, obs, aux, or all
  up        Build and start the local stack (default: full stack)
  debug     Start in Java Debug mode (attaches JDWP on ports 5005-5011)
  down      Stop all local containers
  reset     Stop containers and remove named volumes
  logs      Tail compose logs
  stats     Show one-shot container CPU and memory usage
  ps        Show service status
  build     Build the service images without starting them
  restart   Rebuild and restart all services
EOF
    exit 1
    ;;
esac
