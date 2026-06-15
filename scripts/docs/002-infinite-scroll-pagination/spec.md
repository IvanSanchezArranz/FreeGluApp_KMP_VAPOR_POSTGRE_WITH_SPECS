# Feature Specification: Infinite Scroll Pagination

**Feature Branch**: `002-infinite-scroll-pagination`
**Status**: Draft

## Vision & Product Overview
Users need to smoothly scroll through hundreds of gluten-free products without encountering hard pagination buttons or losing their position. The catalog should dynamically and asynchronously fetch subsequent pages as the user approaches the bottom of the list.

## Technical Architecture

### 1. Presentation Layer (ViewModel)
- Implement `FoodsListViewModel` conforming to Jetpack ViewModel architecture.
- Maintain states: `List<FoodModel>`, `currentPage`, `isLoadingNextPage`, `isEndOfList`, and `error`.
- Support distinct pagination pipelines for both the general catalog and active queries.

### 2. Dependency Injection
- Register `FoodsListViewModel` in `Koin.kt` via `factory`.
- Ensure the ViewModel securely injects `GetAllFoodsUseCase` and `SearchFoodsUseCase`.

### 3. User Interface (Compose)
- Update `FoodsListScreen.kt` to observe the ViewModel's `StateFlow`.
- Attach `rememberLazyGridState()` to the `LazyVerticalGrid`.
- Implement a `LaunchedEffect` that triggers `viewModel.loadNextPage()` when `gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index` is near the total item count.
- Append a loading spinner at the bottom of the grid while `isLoadingNextPage` is true.

## Acceptance Criteria
- [x] Users can scroll to the bottom of the visible items and the app automatically fetches the next 50 items.
- [x] A loading indicator appears at the bottom while fetching.
- [x] Prevents redundant network calls (no duplicate fetches of the same page).
- [x] Filtering/searching resets the list and pagination state back to page 1.
- [x] The feature operates cleanly without crashing or experiencing index-out-of-bounds exceptions.