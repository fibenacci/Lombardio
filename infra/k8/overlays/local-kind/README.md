# Local Kind Overlay

Dieser Overlay ist der lokale Kubernetes-Smoke-Test-Pfad für Lombardio.

Er ist bewusst anders als `base/`:

- keine Vault- oder External-Secrets-Abhängigkeit
- keine Shared-Environment-Ingress-Annahme
- NodePorts für lokalen Browser- und API-Zugriff
- lokale Docker-Images statt Registry-Images

## Ziel

Der Overlay ist für einen ehrlichen lokalen Cluster-Test des aktuellen Stacks gedacht, ohne Compose parallel laufen zu lassen.

## Voraussetzungen

- Docker
- `kind`
- `kubectl`
- lokale Images, z.B. `lombardio-platform:latest`, `lombardio-identity-intelligence:latest`, `lombardio-frontend:latest`

Für das Frontend wird ein Runtime-Image benötigt:

```bash
docker build -f frontend/app/build/package/Dockerfile --target runtime -t lombardio-frontend-runtime:local .
```

## Ablauf

1. Bestehenden Compose-Stack herunterfahren, damit die Host-Ports frei sind.
2. Cluster starten:

```bash
kind create cluster --config infra/kind/local-cluster.yaml
```

3. Lokale Images in den Cluster laden:

```bash
kind load docker-image \
  lombardio-platform:latest \
  lombardio-identity-intelligence:latest \
  lombardio-loan-origination:latest \
  lombardio-pawn-ticket:latest \
  lombardio-auction:latest \
  lombardio-online-auction:latest \
  lombardio-reporting:latest \
  lombardio-frontend-runtime:local \
  --name lombardio-local
```

4. Overlay deployen:

```bash
kubectl kustomize infra/k8/overlays/local-kind --load-restrictor LoadRestrictionsNone | kubectl apply -f -
```

5. Status prüfen:

```bash
kubectl -n lombardio get pods
kubectl -n lombardio get svc
```

6. Headlamp ist direkt über den lokalen Kind-Port erreichbar:

- `http://localhost:8092`

Für den Login in Headlamp kann ein lokales Token erzeugt werden:

```bash
kubectl -n lombardio create token headlamp-admin
```

## Lokale Ports

- Frontend: `http://localhost:8080`
- Keycloak: `http://localhost:8081`
- Platform: `http://localhost:8082`
- Loan Origination: `http://localhost:8083`
- Identity Intelligence unter dem lokalen Service-Namen `customer`: `http://localhost:8084`
- Pawn Ticket: `http://localhost:8085`
- Auction: `http://localhost:8089`
- Online Auction: `http://localhost:8090`
- Reporting: `http://localhost:8091`
- Headlamp: `http://localhost:8092`

## Hinweise

- Das ist ein lokaler Smoke-Test-Pfad, kein Produktions- oder Staging-Overlay.
- Session-Cookies bleiben hier absichtlich `secure=false`, weil über `localhost` ohne HTTPS getestet wird.
- Wenn ein lokales Image fehlt oder veraltet ist, muss es vor `kind load docker-image` neu gebaut werden.
- Wenn sich die Host-Port-Mappings in `infra/kind/local-cluster.yaml` ändern, muss der Kind-Cluster neu erstellt werden.
