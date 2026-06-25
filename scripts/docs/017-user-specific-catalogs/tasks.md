# Actionable Tasks: User-Specific Food Catalogs (Copy-on-Write)

This file contains the exact dependency-ordered checklists and verification commands to implement the User-Specific Food Catalogs feature.

---

## Phase 1: Database and Model Setup

### T1701 DB-02 Crear el modelo y la migración `UserFoodOverride`
*   **Descripción**: Crear la clase del modelo Fluent `UserFoodOverride.swift` y su migración `CreateUserFoodOverride` para generar la tabla de anulaciones y relaciones con usuarios y alimentos. Registrar la migración en `configure.swift`.
*   **Precondiciones**: Ninguna
*   **Estimación**: 3 SP / 1 dev-day
*   **Owner**: @owner-backend
*   **Acceptance Criteria**:
    *   La tabla se genera correctamente al arrancar la app.
    *   No se altera la base de datos preexistente de alimentos.
*   **Test comando**: `cd GlutenFreeAPI && swift build`

---

## Phase 2: Controllers and Logic Integration

### T1702 API-03 Refactorizar `FoodsController` con lógica de Copy-on-Write
*   **Descripción**: Actualizar `getFoods`, `createFood`, `updateFood` y `deleteFood` en `FoodsController.swift` para que lean y escriban sobre la tabla `user_food_overrides` de manera personalizada para el usuario autenticado.
*   **Precondiciones**: T1701
*   **Estimación**: 5 SP / 2 dev-days
*   **Owner**: @owner-backend
*   **Acceptance Criteria**:
    *   Las creaciones de alimentos asocian `food_id = nil`.
    *   La eliminación de un producto base inserta un registro con `is_deleted = true`.
    *   La modificación de un producto base inserta los valores de anulación correspondientes.
*   **Test comando**: `cd GlutenFreeAPI && swift test`
