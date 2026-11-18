#!/bin/bash
cd "$(dirname "$0")"      # always run from e2e-tests/

for s in discovery-server api-gateway user-service product-service media-service order-service; do
  export $(echo $s | tr 'a-z-' 'A-Z_')_IMAGE=$s-image:latest
done

COMPOSE="docker compose -p buymesh-e2e-manual --env-file .env -f docker-compose.e2e.yml"

if $COMPOSE up -d --wait --wait-timeout 300; then
  curl -s http://localhost:18080/actuator/health; echo
  ./mvnw -B test -DbaseUrl=http://localhost:18080
else
  echo "=== STARTUP FAILED ==="
  $COMPOSE ps -a
  $COMPOSE logs --tail=40 kafka    # add other failing services here
fi

$COMPOSE down -v --remove-orphans