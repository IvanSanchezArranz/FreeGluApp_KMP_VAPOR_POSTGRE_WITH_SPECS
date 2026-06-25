# Story: 3-5-database-cloud-migration

## Context & Goal
*   **As a** gluten-free shopper using the production app,
*   **I want to** have the same complete catalog of gluten-free foods on the live cloud server as available locally,
*   **So that** my app has a rich and populated selection of foods out of the box in production.

## References
*   [Source: scripts/docs/015-database-cloud-migration/spec.md]
*   [Source: scripts/docs/015-database-cloud-migration/plan.md]

## Implementation Tasks
*   [x] **Task 1**: Export the local food records from the `foods` table to a SQL data-only dump (`foods_dump.sql`).
*   [x] **Task 2**: Connect to Render's external PostgreSQL host and import the data-only dump cleanly.
*   [x] **Task 3**: Verify the row count matches exactly between local and production.

## Acceptance Criteria
*   [x] **AC-1**: Render database contains the identical food entries as the local development instance.
*   [x] **AC-2**: No table structures or foreign keys are broken during replication.
