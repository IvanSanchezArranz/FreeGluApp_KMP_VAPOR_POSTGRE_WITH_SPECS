# Feature Specification: Product CRUD Management

**Feature Branch**: `013-product-crud-management`
**Status**: Draft (Under Review)

## Vision & Product Overview
To enable continuous catalog updates and community contributions, FreeGluApp needs full Create, Read, Update, and Delete (CRUD) support for product entries. Registered users must be able to add new gluten-free foods, edit existing item metadata (correcting names, ingredients, or brand details), and remove incorrect or discontinued items directly from the client interface, backed by secure API endpoints.

---

## Technical Scope

### 1. Backend Layer (Swift Vapor)
- **`POST /foods`**: Accepts a product request body, creates a new `Food` record with a secure auto-generated UUID, and persists it. Requires authentication.
- **`PUT /foods/:foodID`**: Finds an existing `Food` record, applies updated fields, and saves it. Requires authentication.
- **`DELETE /foods/:foodID`**: Deletes a `Food` record from the database. Requires authentication.
- **Security**: The endpoints are registered in `FoodController.swift` under the `UserMiddleware` group to prevent unauthorized write operations.

### 2. Domain & Data Layers (KMP Shared)
- **Entities & DTOs**: 
  - Add `FoodRequestDTO.kt` representing the input contract for creating or updating a food.
- **Service & Repositories**:
  - Update `ApiService.kt` with `createFood(token: String, food: FoodRequestDTO)`, `updateFood(token: String, id: String, food: FoodRequestDTO)`, and `deleteFood(token: String, id: String)`.
  - Update `FoodRepository` interface and its implementation `FoodRepositoryImpl` to propagate these calls.
- **Use Cases**: Create `CreateFoodUseCase`, `UpdateFoodUseCase`, and `DeleteFoodUseCase` to encapsulate domain-level CRUD rules.

### 3. Presentation Layer (Compose Multiplatform)
- **Form State**: Expose form attributes (code, name, brand, categories, ingredients, imageUrl, isGlutenFree) using a unidirectional State Flow inside `AddEditFoodViewModel`.
- **Form Screen**: Implement `AddEditFoodScreen.kt` as a clean, responsive Material 3 form to handle both "Add" and "Edit" intents seamlessly.
- **Product Detail Controls**: Add "Edit" and "Delete" actions into `FoodDetailScreen.kt` (visible only to logged-in users).
- **Navigation Routing**: Integrate `Screen.AddFood` and `Screen.EditFood(val id: String)` into `App.kt`'s navigation router.

---

## Acceptance Criteria
- [ ] Logged-in users can successfully submit a new product via the "Add Product" form.
- [ ] Users can edit and save changes to any existing product, which instantly updates the catalog.
- [ ] Users can delete a product, which prompts a confirmation dialog and then removes the item from the catalog.
- [ ] Anonymous/logged-out users cannot see the "Add", "Edit", or "Delete" actions.
- [ ] All targets (`iosSimulatorArm64`, `android`, `wasmJs`) compile 100% successfully.
