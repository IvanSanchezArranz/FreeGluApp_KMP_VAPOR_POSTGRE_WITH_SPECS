# Implementation Plan: User-Specific Food Catalogs (Copy-on-Write)

This plan outlines the specific technical steps required to implement the user-specific, non-destructive Copy-on-Write catalog system on the Swift Vapor backend and verify its correctness.

## 🛠️ Step-by-Step Implementation

### Step 1: Create the Fluent model `UserFoodOverride`
1. Create `Sources/GlutenFreeAPI/Models/UserFoodOverride.swift`.
2. Define the schema, ID property, relationships to `User` and `Food` (optional), and the fields mapping the food attributes.
3. Register the model in `configure.swift` and add a migration `CreateUserFoodOverride` to generate the table structure.

### Step 2: Refactor `FoodsController.swift`
1. Update `getFoods` to join `user_food_overrides` for the authenticated user and filter out any items where `is_deleted == true`. Apply any attribute overrides dynamically.
2. Update `createFood` to insert a row into `user_food_overrides` with `food_id = nil` (creating a custom food item).
3. Update `updateFood` to check if the food is base or custom:
   - If custom: edit the `user_food_overrides` row.
   - If base: create a new `user_food_overrides` row with `food_id = :foodID` and the overridden fields.
4. Update `deleteFood` to:
   - If custom: delete the `user_food_overrides` row.
   - If base: create a new `user_food_overrides` row with `food_id = :foodID` and `is_deleted = true`.

### Step 3: Run and Verify Migrations
- Boot the database container or service.
- Compile and run the Vapor server to verify the migrations successfully create the new table.

---

## 🧪 Verification Strategy

### 1. Integration Tests
- Create a test case in `GlutenFreeAPITests.swift`:
  - User A deletes a food item -> Verify it is hidden for User A but visible for User B.
  - User A creates a custom food -> Verify it is visible for User A but hidden for User B.
