Place your local Regula Web API license file here as:

`infra/regula/regula.license`

The local Docker Compose stack mounts this directory into the `regula` container
and copies `regula.license` to the path expected by the Regula runtime on
startup.

This file is ignored by git via `.gitignore`.
