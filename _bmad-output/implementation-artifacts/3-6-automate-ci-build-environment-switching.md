# Story: 3-6-automate-ci-build-environment-switching

## Context & Goal
*   **As a** FreeGluApp developer,
*   **I want to** have the CI/CD pipeline automatically rewrite the environment configuration to `AppEnvironment.AUTO` right before building,
*   **So that** my compiled releases automatically connect to Render on physical devices without needing manual toggling in local source code.

## References
*   [Source: scripts/docs/016-ci-environment-automation/spec.md]
*   [Source: scripts/docs/016-ci-environment-automation/plan.md]

## Implementation Tasks
*   [x] **Task 1**: Integrate pre-build environment rewrite steps in `.github/workflows/release.yml` before compiling Android and iOS.
*   [x] **Task 2**: Integrate the same pre-build environment rewrite step in `.github/workflows/ci.yml` before building KMP.

## Acceptance Criteria
*   [x] **AC-1**: Workflows compile successfully with the automated pre-build rewriting script.
*   [x] **AC-2**: The local file system of the developer remains completely clean and untouched.
