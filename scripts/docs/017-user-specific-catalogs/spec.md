# Feature Specification: User-Specific Food Catalogs (Copy-on-Write)

**Feature Branch**: `017-user-specific-catalogs`
**Status**: Draft (Under Review)

## Vision & Product Overview
Users require a personalized experience where they can modify (edit or delete) products or create their own custom foods without affecting the global catalog of 3,743 foods or other users' views. To achieve this elegantly without massive database bloat (i.e. we *cannot* simply copy all 3,743 rows for every new registered user), we will implement a highly-scalable **Copy-on-Write / Override Architecture**.

---

## Technical Architecture

### 1. Database Schema: `user_food_overrides`
Instead of duplicating the `foods` table, we introduce a new entity `UserFoodOverride`:
*   `id`: UUID (Primary Key)
*   `user_id`: UUID (Foreign Key to `users`)
*   `food_id`: UUID? (Nullable Foreign Key to base `foods`)
*   `is_deleted`: Bool (If `true`, this base food is hidden/deleted for this user).
*   *(And all standard food fields: `code`, `name`, `brand`, `categories`, `ingredients`, `imageUrl`, `countries`, `glutenFree` - all nullable to act as overrides, or fully populated for user-created custom foods).*

### 2. Resolution Logic (Backend View)
When fetching `/foods` for an authenticated user:
1.  **Deletion**: Any base `Food` where `UserFoodOverride.is_deleted == true` matching the `user_id` is filtered out.
2.  **Edition**: If an override exists for a `food_id` with `is_deleted == false`, its non-null fields overwrite the base `Food` fields in the returned JSON.
3.  **Creation**: Any `UserFoodOverride` where `food_id == nil` is treated as a fully custom, private food belonging exclusively to that user.

### 3. API Controller Updates (`FoodsController`)
*   `GET /foods`: Needs to `LEFT JOIN` or fetch user overrides in-memory and merge them before paginating.
*   `POST /foods`: Now inserts into `user_food_overrides` with `food_id = nil`.
*   `PUT /foods/:foodID`: If `:foodID` belongs to a base food, it creates an override in `user_food_overrides` setting the new values.
*   `DELETE /foods/:foodID`: Creates an override setting `is_deleted = true`.

---

## Criterios de Aceptación
- [ ] Un usuario puede eliminar un alimento de la lista base, y este dejará de salir en su app, pero seguirá siendo visible para el resto de usuarios.
- [ ] Un usuario puede crear un nuevo alimento, y solo él podrá visualizarlo.
- [ ] No se duplica la base de datos completa de alimentos por cada nuevo usuario registrado, previniendo el crecimiento O(N*M) de la base de datos (donde N = usuarios, M = alimentos).
