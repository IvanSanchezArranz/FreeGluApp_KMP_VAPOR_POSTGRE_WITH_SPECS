# Feature Specification: Navigation State Preservation

**Feature Branch**: `012-navigation-state-preservation`
**Status**: Approved

## Vision & Product Overview
To provide a cohesive, seamless, and high-quality user experience across all screens of the FreeGluKMP client, we must resolve navigation state loss. When a user navigates to a product detail screen and goes back, they expect to return to their previously active main screen (Catalog or Favorites), the exact category filter they had selected, and the exact scroll position in the list. This avoids frustration and speeds up product discovery.

---

## Technical Scope

### 1. ViewModel Scope Promotion
- **`Koin.kt`**: Promote `FoodsListViewModel` from `factory` to `single`. This ensures that the same instance is retained in memory when navigating to the detail screen and back, preserving the loaded list, query, and selected category.

### 2. Scroll State Retention
- **`FoodsListViewModel.kt`**: Declare `val gridState = LazyGridState()` inside the ViewModel. This acts as a persistent memory of the list's scroll position.
- **`FoodsListScreen.kt`**: Bind `LazyVerticalGrid` to the persistent `viewModel.gridState` rather than using a locally-remembered `rememberLazyGridState()`.

### 3. Bottom Navigation Tab Memory
- **`App.kt`**: Introduce `lastMainScreen` state variable to track whether the active primary tab was "Catalog" (`Screen.List`) or "Favorites" (`Screen.Favorites`).
- **`App.kt`**: Ensure that navigating back from `Screen.Detail` returns the user to `lastMainScreen` instead of always defaulting to `Screen.List`.

---

## Acceptance Criteria
- [ ] Returning from `FoodDetailScreen` to the catalog restores the exact scroll position in `LazyVerticalGrid` (no reset to top).
- [ ] Returning from `FoodDetailScreen` preserves the previously selected category chip (e.g., "Bread", "Pasta").
- [ ] Returning from a product clicked in the "Favorites" tab returns the user to the "Favorites" tab, not the catalog tab.
- [ ] Logging out resets/clears the ViewModel state when required, and KMP modules compile successfully on all multiplatform targets.
