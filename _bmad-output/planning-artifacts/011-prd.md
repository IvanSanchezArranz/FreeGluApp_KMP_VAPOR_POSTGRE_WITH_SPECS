# Feature Specification: App Headers Standardization

**Feature Branch**: `011-app-headers-standardization`
**Status**: Approved

## Vision & Product Overview
To provide a cohesive, modern, and highly accessible user experience across all screens of the FreeGluKMP client, we must standardize and unify all screen headers. This means moving away from the default, bulky, and vertical space-consuming Material `TopAppBar` and `CenterAlignedTopAppBar` components. Instead, we will implement a clean, lightweight, left-aligned, and modern custom header pattern using native Compose `Row` elements (as successfully introduced in the `FoodsListScreen`).

---

## Technical Scope

### 1. Header Conversions
- **`FavoritesScreen.kt` (Favorites Tab)**:
  - Remove `Scaffold` with `CenterAlignedTopAppBar`.
  - Implement a compact, modern, left-aligned custom `Row` header with title "My Favorites" in `MaterialTheme.colorScheme.primary` with `FontWeight.Bold`, matching `FoodsListScreen.kt`.
  - Fix Grid layout by changing `GridCells.Adaptive(minSize = 160.dp)` to `GridCells.Fixed(2)` to guarantee standard card heights and aspect ratios.
- **`FoodDetailScreen.kt` (Product Detail Screen)**:
  - Keep `Scaffold` but style the `TopAppBar` using compact styling and the new modern emerald green theme. Ensure high contrast and high accessibility for back navigation.
  - Set explicit, readable back buttons.

### 2. Sizing & Alignment Standards
- **Header Margins**: Standard padding of `start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp` on all top-level list screens.
- **Header Typographies**: Unified `MaterialTheme.typography.titleLarge` with `FontWeight.Bold` for top-level screens.
- **Grid Layout Symmetrical Alignment**: Both main catalog and favorites screen grids must use `GridCells.Fixed(2)` with `12.dp` spacings.

---

## Acceptance Criteria
- [ ] `FavoritesScreen` has a compact, left-aligned "My Favorites" title, matching the visual weight of `FoodsListScreen`.
- [ ] `FavoritesScreen` cards use standard `GridCells.Fixed(2)` layout, solving card over-stretch and maintaining card height standard.
- [ ] `FoodDetailScreen` top bar uses Ktor/KMP's modern emerald green theme with accessible contrast ratios.
- [ ] Shared module compiles 100% successfully on all multiplatform targets.
