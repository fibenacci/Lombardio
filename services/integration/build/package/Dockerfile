FROM golang:1.26-alpine AS development

WORKDIR /workspace
EXPOSE 8092
CMD ["go", "run", "./cmd/integration-service"]

FROM golang:1.26-alpine AS build

WORKDIR /build
COPY services/integration/go.mod ./go.mod
COPY services/integration/go.sum ./go.sum
COPY services/integration/cmd ./cmd
COPY services/integration/internal ./internal
RUN go build -o /out/integration-service ./cmd/integration-service

FROM alpine:3.22 AS runtime

WORKDIR /app
COPY --from=build /out/integration-service /app/integration-service
EXPOSE 8092
ENTRYPOINT ["/app/integration-service"]
