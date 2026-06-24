# Implementation Tasks: Navigation State Preservation

This task list defines the exact milestones, sprints, and tasks needed to implement and verify the Navigation State Preservation project.

---

## Phase 1: Singleton Scope & Scroll Persistence

### T301 KMP-12 Register FoodsListViewModel as Singleton Scope
*   **Descripción**: Promote `FoodsListViewModel` registration inside `shared/src/commonMain/kotlin/com/ivan/freeglukmp/di/Koin.kt` from a `factory` to a `single` instance. This creates a persistent ViewModel scope.
*   **Precondiciones**: None
*   **Estimación**: 1 SP / 0.5 dev-days
*   **Dependencias**: None
*   **Owner**: @owner-frontend
*   **Acceptance Criteria**:
    *   FoodsListViewModel registration inside `Koin.kt` uses the `single` DSL helper.
    *   The project builds successfully.
*   **Test comando**: `cd FreeGluKMP && ./gradlew :shared:assemble`

### T302 KMP-12 Declare and Bind LazyGridState in ViewModel and View
*   **Descripción**: Create a stable `val gridState = LazyGridState()` inside `FoodsListViewModel.kt` and use it inside `FoodsListScreen.kt` to persist and restore scroll position on navigation transitions.
*   **Precondiciones**: T301
*   **Estimación**: 2 SP / 1.0 dev-days
*   **Dependencias**: T301
*   **Owner**: @owner-frontend
*   **Acceptance Criteria**:
    *   `FoodsListScreen.kt` imports and binds `viewModel.gridState` for `LazyVerticalGrid`.
    *   The local `rememberLazyGridState()` is removed.
    *   Unit tests in `SharedCommonTest.kt` pass and prove state preservation.
*   **Test comando**: `cd FreeGluKMP && ./gradlew :shared:testAndroidHostTest --tests "com.ivan.freeglukmp.SharedCommonTest"`

### T303 KMP-12 Implement Tab and Back-stack Navigation Memory in App Scaffold
*   **Descripción**: Declare `lastMainScreen` state inside `App.kt` to track user navigation between bottom bar destinations (Catalog, Favorites), and update back-navigation callbacks to return to the active main tab.
*   **Precondiciones**: T302
*   **Estimación**: 2 SP / 1.0 dev-days
*   **Dependencias**: T302
*   **Owner**: @owner-frontend
*   **Acceptance Criteria**:
    *   `App.kt` scaffolding implements `lastMainScreen` with tab track on clicks.
    *   `Screen.Detail`'s back-navigation directs to `lastMainScreen`.
    *   Shared module builds successfully.
*   **Test comando**: `cd FreeGluKMP && ./gradlew :shared:assemble`
