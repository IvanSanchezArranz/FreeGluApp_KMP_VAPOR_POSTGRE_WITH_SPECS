# Feature Specification: CI/CD Pipeline Optimization

**Feature Branch**: `004-ci-cd-optimization`
**Status**: Draft

## Vision & Product Overview
Optimize and stabilize the GitHub Actions workflow for Apple Silicon (`macos-14`) runners, resolving property corruptions on gradle memory injections and PostgreSQL setup failures on non-docker platforms.

## Technical Architecture

### 1. Gradle Properties Correction
- Remove the step `echo "org.gradle.jvmargs=-Xmx6G" >> gradle.properties` from `.github/workflows/ci.yml`.
- Rely entirely on the pre-configured `Xmx8G` in our version-controlled `FreeGluKMP/gradle.properties` file.

### 2. Apple Silicon Native PostgreSQL Setup
- Integrate the official action `ikalnytskyi/action-setup-postgres@v8` on the `Backend Tests` job.
- Configure a test database name (`glutenfree`), username (`admin`), and password (`admin`) natively on the runner host.

### 3. Swift Compiler Version Adjustment (Swift 5.10)
- Adjust the setup-swift version from `6.0` to `5.10` to avoid strict Swift 6 concurrency compiler errors on older third-party transitive dependencies (like `swift-nio` or `Fluent`), matching our local, successful development and testing environment.

## Acceptance Criteria
- [x] The KMP Build job compiles successfully without experiencing property parsing failures on `android.useAndroidX`.
- [x] The Backend Tests job successfully spins up a live PostgreSQL instance on the Apple Silicon runner using standardized setup actions.
- [x] The entire GitHub Actions workflow completes successfully with green indicator status.