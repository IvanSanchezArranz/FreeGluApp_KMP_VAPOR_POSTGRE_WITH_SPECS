# Story: 3-4-configure-smart-environments-and-release-pipeline

## Context & Goal
*   **As a** FreeGluApp developer,
*   **I want to** have my client apps automatically detect whether they are running in a local environment (unit test, emulator, simulator, local web) or a production release build (physical phone, live web),
*   **So that** I don't have to manually edit constants or config files before deploying. Additionally, I want a secure CI/CD release pipeline that compiles and publishes `.apk` and iOS release compilation artifacts automatically on pushing tags.

## References
*   [Source: FreeGluKMP/shared/src/commonMain/kotlin/com/ivan/freeglukmp/Platform.kt]
*   [Source: .github/workflows/ci.yml]

## Implementation Tasks
*   [x] **Task 1**: Refactor `getApiBaseUrl()` across Android, iOS, JS, and Wasm targets in KMP to use zero-config automatic environment detection (emulators/simulators/localhost point to local Vapor, physical devices and live web point to Render cloud).
*   [x] **Task 2**: Create `.github/workflows/release.yml` to automatically compile the Android release `.apk` and KMP iOS release frameworks.
*   [x] **Task 3**: Configure the release pipeline to trigger on version tags (e.g. `v*`) or manual execution (`workflow_dispatch`), uploading the compiled binaries as build artifacts and creating a GitHub Release draft.

## Acceptance Criteria
*   [x] **AC-1**: Running on an Android emulator or iOS simulator automatically connects to the local Vapor instance (`10.0.2.2:8080` / `127.0.0.1:8080`).
*   [x] **AC-2**: Running on a physical device automatically connects to `https://freeglu-api.onrender.com`.
*   [x] **AC-3**: Push tags (e.g. `v1.0.0`) trigger the release pipeline, successfully generating the Android `.apk` and compiling KMP iOS frameworks.
