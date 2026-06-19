# Implementation Plan: App Headers Standardization

This plan outlines the specific steps required to standardize all screen headers and grid alignments in `FreeGluKMP`.

## 🛠️ Step-by-Step Implementation

### Step 1: Standardize FavoritesScreen Layout (`FavoritesScreen.kt`)
1.  **Refactor Main Container**: Remove `Scaffold` and its `topBar` block. Instead, wrap the entire screen content in a vertical `Column`.
2.  **Add Compact Custom Header**: Create a `Row` at the top of the column with:
    *   Padding: `start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp`.
    *   Title Text: `"My Favorites"`, styled with `MaterialTheme.typography.titleLarge`, `FontWeight.Bold`, and `color = MaterialTheme.colorScheme.primary`.
3.  **Update Card Grid Layout**: Change `GridCells.Adaptive(minSize = 160.dp)` to `GridCells.Fixed(2)` with `12.dp` spacings to match the standard grid layout of the catalog screen. This solves both card over-stretch and height unevenness.

### Step 2: Refine FoodDetailScreen Header (`FoodDetailScreen.kt`)
1.  **Style TopAppBar**: Ensure Ktor/KMP's modern emerald green theme colors (`MaterialTheme.colorScheme.primary` or `surface`) propagate cleanly to the `TopAppBar`.
2.  **Refine Navigation & Accessibility**:
    *   Back button should use `IconButton` with a high contrast tint.
    *   Align colors for the favorite toggle icon (using `MaterialTheme.colorScheme.primary` when favorited, and high contrast `onSurface` when unfavorited).

---

## 🧪 Verification Strategy

### 1. Compilation check
- Compile and assemble the shared library:
  ```bash
  cd FreeGluKMP && ./gradlew :shared:assemble
  ```
- Verify that Skia, Skiko, and KMP multiplatform compilers generate targets successfully.

### 2. Manual visual verification
- Ensure that the header transition between the Catalog and Favorites tabs is fluid, instantaneous, and shares the exact same height and visual weight.
- Verify that cards on both screens are perfectly symmetrical and aligned.
