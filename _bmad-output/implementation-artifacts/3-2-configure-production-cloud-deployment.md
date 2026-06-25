# Story: 3-2-configure-production-cloud-deployment

## Context & Goal
*   **As a** gluten-free shopper,
*   **I want to** connect my client apps to a live server in the cloud,
*   **So that** I don't have to run a local instance of the database and backend on my machine.

## References
*   [Source: scripts/docs/specs/10_ci_cd_deployment.spec.md]
*   [Source: plans/render-deployment.md]

## Implementation Tasks
*   [x] **Task 1**: Create a detailed plan for deploying the Vapor backend and PostgreSQL to Render.
*   [x] **Task 2**: Add `USE_LOCAL_BACKEND` and `CLOUD_BACKEND_URL` environment/toggle config in `Platform.kt` and apply it across all four multiplatform targets (Android, iOS, JS, Wasm).
*   [x] **Task 3**: Integrate a safe, conditional deployment hook step in the `.github/workflows/ci.yml` pipeline that triggers Render deployment via a secret-secured webhook upon successful tests on `main`.

## Acceptance Criteria
*   [x] **AC-1**: All four platform targets (Android, iOS, JS, Wasm) compile cleanly and respect the unified environment configuration toggle.
*   [x] **AC-2**: The CI workflow executes Swift Vapor tests successfully and includes the deploy trigger step safely.
