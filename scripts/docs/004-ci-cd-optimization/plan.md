# Implementation Plan: CI/CD Pipeline Optimization

**Branch**: `004-ci-cd-optimization`

## Summary
Correct the trailing property corruption on the Gradle build and replace the failing Homebrew PostgreSQL boot commands with the official `action-setup-postgres` setup action.

## Project Structure (Target Files)
```text
FreeGluApp/
├── .github/workflows/ci.yml
└── scripts/docs/004-ci-cd-optimization/
    ├── spec.md
    ├── plan.md
    ├── tasks.md
    └── checklists/requirements.md
```

## Implementation Steps
1. Create spec docs for Spec 004.
2. Edit `.github/workflows/ci.yml`:
   - Replace the entire postgres setup step in the `backend-tests` job with `ikalnytskyi/action-setup-postgres@v8`.
   - Remove the `echo "org.gradle.jvmargs=-Xmx6G" >> gradle.properties` line in `kmp-build` job.
3. Commit and push to GitHub.
4. Verify on GitHub Actions.