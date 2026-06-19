# Story: 2-1-standardize-favorites-screen-layout

## Context & Goal
*   **As a** gluten-free shopper,
*   **I want to** view a clean, left-aligned, compact "My Favorites" header on the Favorites tab,
*   **So that** the top navigation matches the Catalog catalog screen perfectly and saves screen space.

## References
*   [Source: _bmad-output/planning-artifacts/011-prd.md]
*   [Source: _bmad-output/planning-artifacts/011-architecture.md]
*   [Source: _bmad-output/planning-artifacts/011-epics.md#T201]

## Implementation Tasks
*   [ ] **Task 1**: Refactor `FavoritesScreen.kt` to replace `Scaffold` + `CenterAlignedTopAppBar` with a `Column`.
*   [ ] **Task 2**: Add compact left-aligned Row header with `"My Favorites"` title in primary green.
*   [ ] **Task 3**: Update Grid to use standard `GridCells.Fixed(2)` and `12.dp` spacing to align cards.

## Acceptance Criteria
*   [ ] **AC-1**: Header is compact and matches `FoodsListScreen.kt`'s spacing and weights.
*   [ ] **AC-2**: Card grid is set to standard 2-column layout.
*   [ ] **AC-3**: Compiles and builds successfully.
