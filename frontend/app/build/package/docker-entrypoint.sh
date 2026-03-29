#!/bin/sh
set -eu

: "${API_BASE_URL:=http://localhost:8081}"
: "${PLATFORM_API_BASE_URL:=http://localhost:8082}"
: "${ORIGINATION_API_BASE_URL:=http://localhost:8083}"
: "${CUSTOMER_API_BASE_URL:=http://localhost:8084}"
: "${PAWN_TICKET_API_BASE_URL:=http://localhost:8085}"
: "${KYC_API_BASE_URL:=http://localhost:8086}"
: "${AML_API_BASE_URL:=http://localhost:8088}"
: "${AUCTION_API_BASE_URL:=http://localhost:8089}"
: "${ONLINE_AUCTION_API_BASE_URL:=http://localhost:8090}"
: "${REPORTING_API_BASE_URL:=http://localhost:8091}"
: "${KEYCLOAK_BASE_URL:=http://localhost:8080}"
: "${KEYCLOAK_REALM:=lombardio}"
: "${KEYCLOAK_CLIENT_ID:=lombardio-app}"
: "${CENTRIFUGO_WS_URL:=ws://localhost:8000/connection/websocket}"

envsubst \
  '${API_BASE_URL} ${PLATFORM_API_BASE_URL} ${ORIGINATION_API_BASE_URL} ${CUSTOMER_API_BASE_URL} ${PAWN_TICKET_API_BASE_URL} ${KYC_API_BASE_URL} ${AML_API_BASE_URL} ${AUCTION_API_BASE_URL} ${ONLINE_AUCTION_API_BASE_URL} ${REPORTING_API_BASE_URL} ${KEYCLOAK_BASE_URL} ${KEYCLOAK_REALM} ${KEYCLOAK_CLIENT_ID} ${CENTRIFUGO_WS_URL}' \
  < /usr/share/nginx/html/config.template.js \
  > /usr/share/nginx/html/config.js

exec nginx -g 'daemon off;'
