#!/bin/bash
cd "$(dirname "$0")"      # always run from e2e-tests/

# Load the same values compose uses so BASE_URL/ports stay in sync
set -a
. ./.env
set +a

for s in discovery-server api-gateway user-service product-service media-service order-service; do
  export $(echo $s | tr 'a-z-' 'A-Z_')_IMAGE=$s-image:latest
done

COMPOSE="docker compose -p buymesh-e2e-manual --env-file .env -f docker-compose.e2e.yml"
BASE_URL="https://localhost:${E2E_GATEWAY_PORT:-18080}"
RESULT=1

cleanup() {
  $COMPOSE down -v --remove-orphans
}
trap cleanup EXIT

if $COMPOSE up -d --wait --wait-timeout 600; then
  nice -n 19 ./mvnw -B test -DbaseUrl=$BASE_URL 2>&1 | tee e2e-maven.log
  RESULT=${PIPESTATUS[0]}
else
  echo "=== STARTUP FAILED ==="
  $COMPOSE ps -a
  for s in $($COMPOSE ps -a --format '{{.Service}} {{.State}}' | awk '$2!="running"{print $1}'); do
    echo "--- logs: $s ---"; $COMPOSE logs --tail=40 $s
  done
fi

[ $RESULT -eq 0 ] && echo "E2E PASSED" || echo "E2E FAILED (see e2e-compose.log)"
exit $RESULT