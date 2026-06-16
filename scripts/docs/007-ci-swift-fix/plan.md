# Implementation Plan: CI Swift Compiler Crash Fix

**Branch**: `007-ci-swift-fix`

## Summary
Correct the Swift toolchain environment on the macOS-14 runner by removing the redundant/conflicting `swift-actions/setup-swift` action, and explicitly selecting Xcode 16.2 to enable Swift 6 compiler capabilities. This satisfies the compilation requirements of newer transitive dependencies (e.g. `swift-async-algorithms`).

## Project Structure (Target Files)
```text
FreeGluApp/
├── .github/workflows/ci.yml
└── scripts/docs/007-ci-swift-fix/
    ├── spec.md
    ├── plan.md
    ├── tasks.md
    └── checklists/requirements.md
```

## Implementation Steps
1. Create the Spec-Kit documentation structure under `scripts/docs/007-ci-swift-fix/`.
2. Edit `.github/workflows/ci.yml` to:
   - Remove the `Setup Swift` step.
   - Insert the Xcode 16.2 selection step before Setup PostgreSQL.
3. Validate locally and verify that the workflow configuration is correct.
