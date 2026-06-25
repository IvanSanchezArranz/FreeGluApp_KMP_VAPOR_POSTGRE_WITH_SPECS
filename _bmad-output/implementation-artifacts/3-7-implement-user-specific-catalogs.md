# Story: 3-7-implement-user-specific-catalogs

## Context & Goal
*   **As a** registered FreeGluApp user,
*   **I want to** manage my own private and customized foods list starting from the base 3,743 foods,
*   **So that** any modifications or custom foods I add/delete remain private to me and do not affect the listings of other users.

## References
*   [Source: scripts/docs/017-user-specific-catalogs/spec.md]
*   [Source: scripts/docs/017-user-specific-catalogs/plan.md]

## Implementation Tasks
*   [x] **Task 1**: Implement the `UserFoodOverride` Fluent model and registration in `configure.swift`.
*   [x] **Task 2**: Implement the Copy-on-Write catalog merge, create, update, and delete queries in `FoodsController.swift`.
*   [x] **Task 3**: Write an integration test suite validating that deletions and additions are isolated per user.

## Acceptance Criteria
*   [x] **AC-1**: Adding a food creates a custom private item only visible to the creator.
*   [x] **AC-2**: Deleting a base food hides it for the deleting user while leaving it visible for others.
*   [x] **AC-3**: No base database records are deleted from the `foods` table (preserves the original database).
