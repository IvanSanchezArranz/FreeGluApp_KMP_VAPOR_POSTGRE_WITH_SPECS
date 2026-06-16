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

### 2. Workflow & Database Test Environment Adjustment
- **Xcode Toolchain Selection**: Modify `.github/workflows/ci.yml` by removing the conflicting/redundant `Setup Swift` step and inserting a step selecting Xcode 16.2: `sudo xcode-select -s /Applications/Xcode_16.2.app/Contents/Developer` before running tests. This enables native Swift 6 support.
- **Database Schema Auto-Creation**: In the CI environment, the PostgreSQL database starts completely empty. There was no `foods` table, causing queries in tests to fail with `relation "foods" does not exist`.
  - Created a Fluent migration `CreateFood.swift` in `GlutenFreeAPI/Sources/GlutenFreeAPI/Migrations/` to automatically define and create the `foods` table schema.
  - Registered `CreateFood` in `configure.swift`: `app.migrations.add(CreateFood())`.
  - Added dynamic database configurations in `configure.swift` to read from environment variables, defaulting to `127.0.0.1`.
  - Modified the test suite helper `withApp` in `GlutenFreeAPITests.swift` to drop any preexisting `foods` and `_fluent_migrations` tables using SQLKit raw queries, and then run `try await app.autoMigrate()`. This guarantees a completely clean, identical database schema state locally and in CI.
- **Swift 6 Existential Any Enforcement**: Since we selected Xcode 16.2 / Swift 6 for compiling, the compiler strictly enforces the `#ExistentialAny` feature as an error rather than a warning.
  - Updated `CreateFood.swift` to use `any Database` in `prepare` and `revert` methods.
  - Updated `GlutenFreeAPITests.swift` to use `as? any SQLDatabase` for the database cast. This fully aligns our database setup with Swift 6 standards and eliminates all compiler errors.

## Acceptance Criteria
- [x] `.github/workflows/ci.yml` is updated to rely on the runner's native Xcode/Swift toolchain.
- [x] Create and register `CreateFood` migration to auto-create the database schema.
- [x] Update test environment `withApp` helper to reset the database and execute auto-migrate on every test run.
- [x] The `backend-tests` job completes successfully without compilation crashes or database relation errors.
