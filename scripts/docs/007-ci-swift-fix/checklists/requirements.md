# Requirements: CI Swift Compiler Crash Fix

## Business Requirements
- Ensure that the automated CI/CD pipeline executes successfully on GitHub Actions.
- Prevent environment-induced Swift compiler crashes during module emission (`_CryptoExtras`).

## Acceptance Criteria
- [x] Remove `swift-actions/setup-swift` from `.github/workflows/ci.yml` on the macOS runner to avoid Swift toolchain conflicts with native SDKs.
- [x] Ensure the native preinstalled Xcode toolchain is utilized for the Vapor build.
- [x] Verify that the pipeline builds and executes all backend tests successfully.
