#!/bin/bash
cd "$(dirname "$0")"      # always run from e2e-tests/

for s in discovery-server api-gateway user-service product-service media-service order-service; do
  export $(echo $s | tr 'a-z-' 'A-Z_')_IMAGE=$s-image:latest
done

COMPOSE="docker compose -p buymesh-e2e-manual --env-file .env -f docker-compose.e2e.yml"
BASE_URL="http://localhost:18080"
RESULT=1

cleanup() {
  $COMPOSE logs --no-color > e2e-compose.log 2>&1   # keep logs for debugging
  $COMPOSE down -v --remove-orphans
}
trap cleanup EXIT

if $COMPOSE up -d --wait --wait-timeout 300; then
  nice -n 19 ./mvnw -B test -DbaseUrl=$BASE_URL 2>&1 | tee e2e-maven.log
  RESULT=${PIPESTATUS[0]}
else
else
  echo "=== STARTUP FAILED ==="
  $COMPOSE ps -a
  for s in $($COMPOSE ps -a --format '{{.Service}} {{.State}}' | awk '$2!="running"{print $1}'); do
    echo "--- logs: $s ---"; $COMPOSE logs --tail=40 $s
  done
fi

[ $RESULT -eq 0 ] && echo "E2E PASSED" || echo "E2E FAILED (see e2e-compose.log)"
exit $RESULT