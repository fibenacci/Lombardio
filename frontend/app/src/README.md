# Frontend Architecture

Dieses Verzeichnis folgt einem klaren `app / shared / modules`-Schnitt.

## Zielbild

- `app/`
  Bootstrapping, Router, Provider, Session- und Security-Flows.
- `shared/`
  Technische Kernel-Bausteine und wiederverwendbare UI-Grundbausteine ohne Fachlogik.
- `modules/`
  Fachliche Features mit innerer hexagonaler Struktur.
- `test/`
  Frontend-Tests außerhalb der Fachmodule, aber direkt gegen `app/`, `shared/` und `modules/`.

## Modulstruktur

Jedes nicht-triviale Feature soll diesem Schnitt folgen:

```txt
modules/<feature>/
  domain/
    model/
    ports/
    policies/
  application/
    dto/
    services/
  infrastructure/
    adapters/
    dto/
    mappers/
  state/
  ui/
    composables/
    components/
    pages/
```

## Schichtenregeln

- `domain/`
  Framework-agnostisch, klein, stabil, ohne PrimeVue oder HTTP.
- `application/`
  Use Cases, Commands, Queries, Orchestrierung gegen Ports.
- `infrastructure/`
  REST, HTTP, DTOs und Mapper.
- `state/`
  Nur App- oder Feature-State. Keine Infrastruktur-Logik.
- `ui/composables/`
  Wiederverwendbare, UI-nahe Orchestrierung.
- `ui/components/`
  Schmale Render-Komponenten ohne DTO-Importe und ohne direkte API-Calls.
- `ui/pages/`
  Composition Roots für Routen.

## Importregeln

- Nutze bevorzugt öffentliche Einstiegspunkte:
  - `shared/ui/base`
  - `shared/ui/feedback`
  - `modules/<feature>/ui/components`
- Importiere DTOs nie direkt in `ui/`.
- Neue PrimeVue-Nutzung nur in `shared/ui` oder in App-Providern.

## Migration

Wenn Altcode angepasst wird:

1. Implementierung nach `app/`, `shared/` oder `modules/` verschieben.
2. Tests unter `test/` gegen den neuen Zielpfad ausrichten.
3. Legacy-Rootpfade nicht weiterführen oder neu einführen.
4. Neue Logik nur noch in `app/`, `shared/` oder `modules/` anlegen.
