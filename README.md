# World Seed project template

This repository contains a minimal full‑stack template aligned with the local Cursor rules:

- **Frontend**: TypeScript + React + Vite (+ Redux Toolkit)
- **Backend**: Java + Spring Boot + Maven (+ Liquibase, springdoc-openapi)

Folders:

- `frontend` – SPA client application.
- `backend` – REST backend and persistence layer.

You can treat this as a starting point and evolve it as needed.

## Terrain compression (backend)
Backend can optimize generated `.glb` terrains before uploading to MinIO via external CLI.

Commands follow [@gltf-transform/cli](https://www.npmjs.com/package/@gltf-transform/cli) (geometry compression uses the `meshopt` / `draco` subcommands, not only `optimize`).

### Install CLI
```bash
npm install --global @gltf-transform/cli
```
Check installation:
```bash
gltf-transform --version
```

Example invocations (same as in the package readme):

```bash
gltf-transform meshopt input.glb output.glb --level medium
gltf-transform draco input.glb output.glb --method edgebreaker
```

On Windows, `gltf-transform` from npm is a `.cmd` shim. The backend wraps the configured command with `cmd.exe /c` so Java can find it the same way as in a terminal. If you still see “Cannot find file”, ensure the JVM process inherits `PATH` (including the npm global folder, often `%AppData%\npm`) — e.g. start the IDE from a shell where `gltf-transform --version` works, or add that folder to the system/user `PATH`.

