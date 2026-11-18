#!/bin/bash

for s in discovery-server api-gateway user-service product-service media-service order-service; do
  export $(echo $s | tr 'a-z-' 'A-Z_')_IMAGE=$s-image:latest
done

COMPOSE="docker compose -p buymesh-e2e-manual --env-file .env -f docker-compose.e2e.yml"
$COMPOSE up -d --wait --wait-timeout 300 || $COMPOSE ps
curl -s http://localhost:18080/actuator/health
./e2e-tests/mvnw -f e2e-tests/pom.xml test -DbaseUrl=http://localhost:18080

$COMPOSE down -v --remove-orphans    # always clean up