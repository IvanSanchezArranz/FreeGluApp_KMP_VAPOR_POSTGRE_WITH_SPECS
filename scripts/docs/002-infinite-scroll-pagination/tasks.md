# Tasks: Infinite Scroll Pagination

- [x] T001: Create FoodsListViewModel in `shared/src/commonMain/kotlin/com/ivan/freeglukmp/presentation/list/FoodsListViewModel.kt` with coroutines flow, page accumulation, loading state, end-of-list detection, search query input and category filter.
- [x] T002: Register FoodsListViewModel in `Koin.kt` module.
- [x] T003: Refactor `FoodsListScreen.kt` to use the new FoodsListViewModel via `koinInject()` or direct inject.
- [x] T004: Bind the scroll detection via LazyGridState in `FoodsListScreen.kt` to load subsequent pages, and add a loading indicator at the bottom of the list.
- [x] T005: Validate the functionality using Gradle builds and KMP test suites.