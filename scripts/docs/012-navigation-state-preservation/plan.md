# Implementation Plan: Navigation State Preservation

This plan outlines the specific steps required to preserve the category tab, list scroll position, and bottom navigation screen tab across screen switches in `FreeGluKMP`.

## 🛠️ Step-by-Step Implementation

### Step 1: Promote FoodsListViewModel to Singleton (`Koin.kt`)
1. **Refactor Registration**: Inside `shared/src/commonMain/kotlin/com/ivan/freeglukmp/di/Koin.kt`, replace the `factory` definition for `FoodsListViewModel` with a `single` definition.
2. **Impact**: This keeps the active `FoodsListViewModel` alive across screen navigation, preventing the loaded list of products, active query, and active category tab from resetting.

### Step 2: Persist Grid Scroll State inside ViewModel (`FoodsListViewModel.kt` & `FoodsListScreen.kt`)
1. **Declare state in ViewModel**: Create a stable `val gridState = LazyGridState()` property inside `FoodsListViewModel.kt` to act as the persistent scroll state.
2. **Use State in UI**: Inside `FoodsListScreen.kt`, remove the local `val gridState = rememberLazyGridState()` and replace it with `val gridState = viewModel.gridState`. All layout calculations and scroll bindings (such as derived states) will automatically hook into the persistent state.

### Step 3: Implement Back-stack Memory for Main Bottom Tabs (`App.kt`)
1. **Declare Active Tab Memory**: Create a `lastMainScreen` state variable inside `App.kt` initialized to `Screen.List` or the user's initial screen.
2. **Update Active Tab**: Every time the bottom navigation items are clicked, update `lastMainScreen` with the selected screen (`Screen.List` or `Screen.Favorites`).
3. **Configure Back Navigation**: Update `Screen.Detail`'s back-callback (`onNavigateBack`) to set `currentScreen` to `lastMainScreen` instead of a hardcoded `Screen.List`.

---

## 🧪 Verification Strategy

### 1. Compilation & Unit Testing
- Compile and assemble the shared library for all targets:
  ```bash
  cd FreeGluKMP && ./gradlew :shared:assemble
  ```
- Run the shared module unit tests:
  ```bash
  cd FreeGluKMP && ./gradlew :shared:allTests
  ```

### 2. Manual UX Verification
- Select a category (e.g. "Pasta"), scroll down the grid to find a product, tap on it to view details.
- Tap the back button and verify that:
  - The "Pasta" tab is still selected.
  - The scroll position remains exactly aligned to the tapped product.
- Select the "Favorites" tab, tap on a favorited product, tap back, and verify the app returns to "Favorites" rather than "Catalog".
