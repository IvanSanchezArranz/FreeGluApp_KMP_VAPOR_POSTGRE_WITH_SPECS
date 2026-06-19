# Story: 2-2-refine-detail-screen-header

## Context & Goal
*   **As a** gluten-free shopper,
*   **I want to** view a compact, custom, modern Row-based header on the product detail view containing the Back button, the title, and the Favorite heart button aligned horizontally,
*   **So that** the detail header shares the same slim visual height, padding, and layout as other screens, maximizing product content space.

## References
*   [Source: _bmad-output/planning-artifacts/011-prd.md]
*   [Source: _bmad-output/planning-artifacts/011-architecture.md]
*   [Source: _bmad-output/planning-artifacts/011-epics.md#T202]

## Implementation Tasks
*   [x] **Task 1**: Refactor `FoodDetailScreen.kt` to remove the bulky `Scaffold` and `TopAppBar`.
*   [x] **Task 2**: Add a compact, custom Row header containing:
    *   Leftmost: Back button (ArrowBack icon, primary emerald green tinted).
    *   Center-Left: `"Product Details"` title text in primary green, bold.
    *   Rightmost: Favorite heart toggle button.
*   [x] **Task 3**: Compile and verify layout with gradle assemble.

## Acceptance Criteria
*   [x] **AC-1**: Detail header is a custom compact row aligned horizontally (removes bulky TopAppBar).
*   [x] **AC-2**: Back button, Details title, and Favorite toggle align on a single slim line.
*   [x] **AC-3**: Shared module compiles 100% successfully on all platforms.
