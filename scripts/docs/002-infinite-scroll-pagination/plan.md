# Implementation Plan: Infinite Scroll Pagination

**Branch**: `002-infinite-scroll-pagination`

## Summary
Implement a seamless, infinite-scrolling catalog by decoupling state management into a Koin-injected ViewModel and utilizing Compose's `LazyGridState` to preemptively fetch subsequent pages.

## Technical Context
**Language/Version**: Kotlin 2.4.0 / Compose Multiplatform
**Primary Dependencies**: Koin Compose ViewModel, Ktor Client
**Target Platform**: Android, iOS, Web (WasmJs)

## Project Structure (Target Files)
```text
FreeGluKMP/
└── shared/src/commonMain/kotlin/com/ivan/freeglukmp/
    ├── presentation/
    │   ├── list/
    │   │   ├── FoodsListViewModel.kt    # NEW
    │   │   └── FoodsListScreen.kt       # MODIFIED
    └── di/
        └── Koin.kt                      # MODIFIED
```

## Implementation Steps
1. **Create ViewModel:** Build `FoodsListViewModel.kt` to handle asynchronous pagination logic, managing offsets (`page`, `per`), and accumulating the `List<FoodModel>`. It will handle both general list pagination and search/category filtered pagination.
2. **Setup DI:** Configure standard Koin factory registration in `Koin.kt` for the new ViewModel.
3. **Refactor UI:** Bind `FoodsListScreen` to the ViewModel instead of holding local state.
4. **Scroll Detection:** Implement derived state logic from `LazyGridState` to dispatch load events when the scroll threshold is crossed (e.g., 5 items before the end).
5. **Review & Test:** Ensure scrolling gracefully degrades upon reaching the end of the remote dataset, and that changing filters resets the pagination state correctly.

## Verification & Testing
1. Launch the app and observe the initial load.
2. Scroll to the bottom of the grid and verify that a loading indicator appears and more items are loaded without user interaction.
3. Verify that rapid scrolling does not trigger duplicate API calls for the same page.
4. Perform a search or select a category and verify that the list is cleared, pagination resets to page 1, and new items are loaded correctly.