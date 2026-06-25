# Story: 3-3-implement-multi-environment-toggles

## Context & Goal
*   **As a** FreeGluApp developer,
*   **I want to** have a clear, centralized type-safe toggle to switch my entire client application between `LOCAL` development and `PRODUCTION` cloud backends,
*   **So that** I can easily develop locally against my local Vapor backend and safely deploy production builds to Render without manual URL rewriting.

## References
*   [Source: FreeGluKMP/shared/src/commonMain/kotlin/com/ivan/freeglukmp/Platform.kt]
*   [Source: plans/render-deployment.md]

## Implementation Tasks
*   [x] **Task 1**: Implement the `AppEnvironment` enum class in `commonMain` declaring `LOCAL` and `PRODUCTION` values.
*   [x] **Task 2**: Refactor `Platform.kt` and its target-specific platform implementations (Android, iOS, JS, Wasm) to determine their base API URLs dynamically based on the configured environment.
*   [x] **Task 3**: Verify that local unit tests always point to the local server seamlessly even when toggled.

## Acceptance Criteria
*   [x] **AC-1**: Changing `CURRENT_ENVIRONMENT` to `LOCAL` points all platform clients to the local Vapor instance (`127.0.0.1:8080` / `10.0.2.2:8080`).
*   [x] **AC-2**: Changing `CURRENT_ENVIRONMENT` to `PRODUCTION` points all platform clients to `https://freeglu-api.onrender.com`.
*   [x] **AC-3**: KMP and Vapor test suites compile and pass successfully.
