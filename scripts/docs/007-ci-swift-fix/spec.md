# Feature Specification: CI Swift Compiler Crash Fix

**Feature Branch**: `007-ci-swift-fix`
**Status**: Proposal

## Vision & Product Overview
Resolve the fatal compilation crash (`error: fatalError` during emitting module `_CryptoExtras`) occurring in the GitHub Actions CI workflow on the `macos-14` runner.

## Technical Architecture

### 1. Root Cause Analysis
- The GitHub Actions `macos-14` runner comes with preinstalled Xcode versions (including Xcode 15.4 with Swift 5.10 as default, and Xcode 16.2 with Swift 6.2).
- The package dependency `swift-async-algorithms` (a transitive dependency of our Vapor backend) has been updated to utilize modern Swift 6 features, specifically dynamic actor isolation with `isolated (any Actor)?` and the `#isolation` macro.
- Trying to compile this dependency with the default Xcode 15.4 (Swift 5.10) compiler triggers severe syntax and semantic errors such as:
  - `error: 'isolated' parameter has non-actor type '(any Actor)?'`
  - `error: 'nil' requires a contextual type`
- To support these modern Swift 6 language features, we must select Xcode 16.2 as our active compiler toolchain on the `macos-14` runner.

### 2. Workflow Adjustment
- Modify `.github/workflows/ci.yml`:
  - Remove the conflicting/redundant `Setup Swift` step.
  - Insert a step selecting Xcode 16.2: `sudo xcode-select -s /Applications/Xcode_16.2.app/Contents/Developer` before running tests. This enables Swift 6 support.

## Acceptance Criteria
- [x] `.github/workflows/ci.yml` is updated to rely on the runner's native Xcode/Swift toolchain.
- [x] The `backend-tests` job completes successfully without compilation crashes.
