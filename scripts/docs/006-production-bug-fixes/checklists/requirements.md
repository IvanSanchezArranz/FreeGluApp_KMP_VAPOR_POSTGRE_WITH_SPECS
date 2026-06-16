# Requirements: Production Bug Fixes

## Business Requirements
- The mobile application must run on Android without startup crashes caused by uninitialized Koin contexts.
- The backend API must serve database records successfully without throwing deserialization or type mismatch errors on Postgres BigInt.

## Acceptance Criteria
- [x] Android app loads the foods list screen without Koin exceptions.
- [x] GET `/foods` queries Postgres successfully and responds with valid JSON.
