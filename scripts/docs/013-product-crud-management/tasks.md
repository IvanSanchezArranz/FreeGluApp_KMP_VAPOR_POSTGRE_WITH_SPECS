# Implementation Tasks: Product CRUD Management

This task list defines the exact milestones, sprints, and tasks needed to implement and verify the Product CRUD Management feature.

---

## Phase 1: Backend APIs (Swift Vapor)

### T401 VAP-01 Implement Food CRUD Endpoints inside FoodController
*   **Descripción**: Update `FoodController.swift` inside Vapor backend to support JWT authenticated CRUD: `POST /foods`, `PUT /foods/:foodID`, and `DELETE /foods/:foodID`.
*   **Precondiciones**: None
*   **Estimación**: 3 SP / 1.5 dev-days
*   **Dependencias**: None
*   **Owner**: @owner-backend
*   **Acceptance Criteria**:
    *   `POST /foods` successfully creates a product.
    *   `PUT /foods/:foodID` updates product properties.
    *   `DELETE /foods/:foodID` removes the product.
    *   All write routes return 401 Unauthorized if the token is missing/invalid.
*   **Test comando**: `cd GlutenFreeAPI && swift test`

---

## Phase 2: KMP Network & Domain Layers

### T402 KMP-13 Implement FoodRequestDTO and Extend ApiService
*   **Descripción**: Create `FoodRequestDTO.kt` in `commonMain` and add CRUD network methods inside `ApiService.kt` using Ktor HTTP client.
*   **Precondiciones**: T401
*   **Estimación**: 2 SP / 1.0 dev-days
*   **Dependencias**: T401
*   **Owner**: @owner-frontend
*   **Acceptance Criteria**:
    *   `FoodRequestDTO` is fully serializable and correct.
    *   `ApiService` calls include the Bearer authentication token.
*   **Test comando**: `cd FreeGluKMP && ./gradlew :shared:assemble`

### T403 KMP-13 Create CRUD Use Cases and Register in Koin
*   **Descripción**: Implement `CreateFoodUseCase`, `UpdateFoodUseCase`, and `DeleteFoodUseCase` inside `domain/usecase/`. Update `Koin.kt` module registration.
*   **Precondiciones**: T402
*   **Estimación**: 2 SP / 1.0 dev-days
*   **Dependencias**: T402
*   **Owner**: @owner-frontend
*   **Acceptance Criteria**:
    *   Use cases are registered in Koin successfully.
    *   Unit tests in `SharedCommonTest.kt` pass.
*   **Test comando**: `cd FreeGluKMP && ./gradlew :shared:testAndroidHostTest --tests "com.ivan.freeglukmp.SharedCommonTest"`

---

## Phase 3: Presentation & Navigation

### T404 KMP-13 Implement AddEditFoodViewModel and Screen Form
*   **Descripción**: Create `AddEditFoodViewModel` and `AddEditFoodScreen.kt` in Compose Multiplatform to provide a rich input form for creating and updating product records.
*   **Precondiciones**: T403
*   **Estimación**: 4 SP / 2.0 dev-days
*   **Dependencias**: T403
*   **Owner**: @owner-frontend
*   **Acceptance Criteria**:
    *   Form handles validation errors (e.g. empty fields).
    *   Correctly supports both creation (Add) and editing (Edit) of foods.
*   **Test comando**: `cd FreeGluKMP && ./gradlew :shared:assemble`

### T405 KMP-13 Update FoodDetailScreen with Actions and App Navigator
*   **Descripción**: Update `FoodDetailScreen.kt` to include authenticated edit and delete actions. Update `App.kt` routing navigation branches to include add/edit flows.
*   **Precondiciones**: T404
*   **Estimación**: 3 SP / 1.5 dev-days
*   **Dependencias**: T404
*   **Owner**: @owner-frontend
*   **Acceptance Criteria**:
    *   Edit/Delete icons visible only when logged in.
    *   App compiles successfully on iOS Simulator, Android, and Web Wasm.
*   **Test comando**: `cd FreeGluKMP && ./gradlew :shared:assemble`

### T406 KMP-13 Implement Auto-Logout on Token Expiration
*   **Descripción**: Ensure that when a 401 Unauthorized response is caught in the `FoodRepositoryImpl` (due to an expired or invalid token), the application automatically triggers a logout state broadcast, clearing local storage and instantly redirecting the user back to the Login screen.
*   **Precondiciones**: T405
*   **Estimación**: 2 SP / 1.0 dev-days
*   **Dependencias**: T405
*   **Owner**: @owner-frontend
*   **Acceptance Criteria**:
    *   Catching 401 triggers `authRepository.logout()`.
    *   `App.kt` listens to `authRepository.isLoggedInState` flow.
    *   The client automatically redirects from protected screens to the Login screen upon token expiration.
*   **Test comando**: `cd FreeGluKMP && ./gradlew :shared:testAndroidHostTest --tests "com.ivan.freeglukmp.SharedCommonTest"`
