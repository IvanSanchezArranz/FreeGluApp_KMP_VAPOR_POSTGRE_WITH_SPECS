# Story: 3-1-fix-favorites-session-loading

## Context & Goal
*   **As a** gluten-free shopper with a cached session,
*   **I want to** have my favorites load and save correctly even if the server database has been reset or the user is deleted,
*   **So that** I don't experience server errors or foreign key constraint violations during my session.

## References
*   [Source: GlutenFreeAPI/Sources/GlutenFreeAPI/Controllers/AuthController.swift]
*   [Source: specs/001-user-auth/spec.md]

## Implementation Tasks
*   [x] **Task 1**: Refactor `UserMiddleware` in `AuthController.swift` on the Swift Vapor server to verify that the `userID` parsed from the JWT payload actually exists in the `users` database table.
*   [x] **Task 2**: Throw a `401 Unauthorized` error (e.g. `Bearer token invalid or expired` or a dedicated session error) if the authenticated user is not found in the database.
*   [x] **Task 3**: Ensure KMP client handles `401 Unauthorized` responses gracefully by clearing the invalid token, resetting the cached session, and prompting the user to log in again.

## Acceptance Criteria
*   [x] **AC-1**: Submitting a request (e.g. `POST /favorites/:foodID`) with an invalid or orphaned JWT token returns `401 Unauthorized` instead of triggering a database foreign key constraint violation (PSQLError 23503).
*   [x] **AC-2**: Server and client modules compile and pass all tests successfully.
