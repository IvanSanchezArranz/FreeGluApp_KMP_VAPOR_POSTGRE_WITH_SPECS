# Story: 1-3-fix-food-card-height-unevenness

## Context & Goal
*   **As a** gluten-free shopper,
*   **I want to** view product cards in the grid that have a completely standard, uniform height,
*   **So that** the layout is balanced, visually clean, and easy to read.

## References
*   [Source: _bmad-output/planning-artifacts/prd.md]
*   [Source: _bmad-output/planning-artifacts/DESIGN.md]
*   [Source: _bmad-output/planning-artifacts/epics.md#T110]

## Implementation Tasks
*   [x] **Task 1**: Update `FoodCard.kt`'s product name Text component to use `minLines = 2` and `maxLines = 2` (ensuring 1-line and 2-line titles reserve the exact same space).
*   [x] **Task 2**: Compile and verify layout with gradle assemble.

## Acceptance Criteria
*   [x] **AC-1**: All product cards in the grid have identical height regardless of title length.
*   [x] **AC-2**: Shared module compiles 100% successfully on all multiplatform targets.
