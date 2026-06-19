# Implementation Tasks: App Headers Standardization

This task list defines the exact milestones, sprints, and tasks needed to implement and verify the App Headers Standardization project.

---

## Phase 1: Header & Grid Refactor

### T201 KMP-09 Standardize FavoritesScreen Header & Grid Sizing
*   **Descripción**: Refactor `FavoritesScreen.kt` to replace `Scaffold` and `CenterAlignedTopAppBar` with a vertical `Column` containing a compact custom `Row` header. Set grid layout columns to `GridCells.Fixed(2)` with `12.dp` spacings to match the Catalog grid perfectly.
*   **Precondiciones**: None
*   **Estimación**: 3 SP / 1.5 dev-days
*   **Dependencias**: None
*   **Owner**: @owner-frontend
*   **Acceptance Criteria**:
    *   `FavoritesScreen` header is compact and left-aligned, matching `FoodsListScreen` perfectly in padding and font weight.
    *   Grid uses `GridCells.Fixed(2)` with `12.dp` spacings.
    *   Shared module compiles successfully.
*   **Test comando**: `cd FreeGluKMP && ./gradlew :shared:assemble`

### T202 KMP-09 Refine FoodDetailScreen Header to Compact Row Layout
*   **Descripción**: Refactor `FoodDetailScreen.kt` to replace the bulky `Scaffold` and `TopAppBar` with a vertical `Column` containing a compact custom `Row` header. Place the Back button, "Product Details" title, and Favorite heart button in a single slim, modern horizontal row, matching the spacing and visual height of other screens.
*   **Precondiciones**: T201
*   **Estimación**: 3 SP / 1.5 dev-days
*   **Dependencias**: T201
*   **Owner**: @owner-frontend
*   **Acceptance Criteria**:
    *   `FoodDetailScreen` header is a custom compact row (removes bulky TopAppBar/Scaffold).
    *   Back button, Title, and Favorite heart button align on a single slim horizontal line.
    *   Shared module compiles successfully on all platforms.
*   **Test comando**: `cd FreeGluKMP && ./gradlew :shared:assemble`
