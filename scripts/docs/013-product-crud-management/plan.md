# Implementation Plan: Product CRUD Management

This plan outlines the specific steps required to implement Full-Stack Create, Update, and Delete capabilities for products in `FreeGluApp`.

## 🛠️ Step-by-Step Implementation

### Step 1: Extend Backend APIs (`GlutenFreeAPI`)
1. **Define Input DTO**: Inside `FoodController.swift`, define a decodable struct `CreateFoodInput: Content` mapping to all writeable attributes.
2. **Add Endpoints**:
   * Wrap write routes under the JWT protected routing group using `UserMiddleware`.
   * Implement `POST /foods` to validate inputs, instantiate a new `Food` record, and save it.
   * Implement `PUT /foods/:foodID` to find a record, mutate its parameters, and save it.
   * Implement `DELETE /foods/:foodID` to find a record and remove it.

### Step 2: Extend KMP Shared Network and Repository Layer
1. **Create FoodRequestDTO**: Add a Kotlin `@Serializable` data class representing the create/update request payload.
2. **Update ApiService**: Add POST, PUT, and DELETE HTTP client methods injecting the active JWT Bearer token inside headers.
3. **Update Repository**: Declare and implement CRUD actions inside the `FoodRepository` interface and its implementation `FoodRepositoryImpl`.

### Step 3: Implement Domain Use Cases & Koin Registration
1. **Create Use Cases**:
   * `CreateFoodUseCase`: Injects `FoodRepository` and executes `createFood`.
   * `UpdateFoodUseCase`: Injects `FoodRepository` and executes `updateFood`.
   * `DeleteFoodUseCase`: Injects `FoodRepository` and executes `deleteFood`.
2. **Koin Injections**: Update `di/Koin.kt` to register the new Use Cases and the new `AddEditFoodViewModel`.

### Step 4: Implement Compose Forms and ViewModel
1. **Create AddEditFoodViewModel**: Expose form state flow, handle input validations (checking that name, code, brand are not blank), and manage async creation/update API execution.
2. **Create AddEditFoodScreen**: Render a modern, accessible form with descriptive text fields, validation error messages, and save buttons.
3. **Refine FoodDetailScreen**:
   * Render edit and delete action buttons conditionally based on user login status.
   * Connect delete action to a confirmation dialog.

### Step 5: Update App Routing (`App.kt`)
1. **Update Screen Enum**: Add `Screen.AddFood` and `Screen.EditFood(val id: String)` parameters.
2. **Update Navigator**: Support transition to these new screens in the main navigation branch.

---

## 🧪 Verification Strategy

### 1. Backend Endpoint Tests
- Create tests inside Swift Vapor test suite checking authentication middleware enforcement, successful creation, safe validation errors, and deletion.

### 2. Compilation check
- Compile KMP target modules to confirm zero multiplatform compiler errors:
  ```bash
  cd FreeGluKMP && ./gradlew :shared:assemble
  ```

### 3. Manual Functional Flow
- Run the local server and clients.
- Verify creation, update, and deletion cycles on all platform targets (Android, iOS, WebAssembly).
