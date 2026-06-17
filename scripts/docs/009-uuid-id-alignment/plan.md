# Implementation Plan: UUID ID Type Alignment

This plan outlines the specific steps required to implement and verify the UUID ID alignment across the backend and the KMP client layers.

## 🛠️ Step-by-Step Implementation

### Step 1: Backend Swift Vapor Updates
1.  **Modify `Food.swift` (`GlutenFreeAPI/Sources/GlutenFreeAPI/Models/Food.swift`)**:
    *   Change `import Vapor` and `import Fluent` to also include `import Foundation` if needed (it is already included indirectly, but good to ensure).
    *   Change property wrapper of `id` from `@ID(custom: .id, generatedBy: .database)` to `@ID(key: .id)`.
    *   Change field type from `Int?` to `UUID?`.
    *   Update initializer parameters to `id: UUID? = nil` and ensure body assigns `self.id = id`.
2.  **Modify `CreateFood.swift` (`GlutenFreeAPI/Sources/GlutenFreeAPI/Migrations/CreateFood.swift`)**:
    *   Change `.field(.id, .int, .identifier(auto: true))` to `.id()` which automatically configures a standard `.uuid` primary key.
3.  **Modify `GlutenFreeAPITests.swift` (`GlutenFreeAPI/Tests/GlutenFreeAPITests/GlutenFreeAPITests.swift`)**:
    *   Change `let invalidId = 999999` to `let invalidId = UUID()`.

### Step 2: Kotlin Multiplatform Client Updates
1.  **Modify `DTOs.kt` (`FreeGluKMP/shared/src/commonMain/kotlin/com/ivan/freeglukmp/data/remote/DTOs.kt`)**:
    *   Change `val id: Int? = null` inside `FoodDTO` to `val id: String? = null`.
2.  **Modify `Mappers.kt` (`FreeGluKMP/shared/src/commonMain/kotlin/com/ivan/freeglukmp/data/remote/Mappers.kt`)**:
    *   Change `id = this.id?.toString() ?: ""` to `id = this.id ?: ""`.

### Step 3: Implement Image Loading & Placeholder States (Coil 3)
1.  **Modify `FoodCard.kt` (`FreeGluKMP/shared/src/commonMain/kotlin/com/ivan/freeglukmp/presentation/components/FoodCard.kt`)**:
    *   Replace `AsyncImage` with `SubcomposeAsyncImage`.
    *   Add a loading state showing a centrated `CircularProgressIndicator(modifier = Modifier.scale(0.5f))` or simple loading spinner.
    *   Add an error/fallback state showing a styled gray/neutral Box with an `Icons.Default.ShoppingCart` vector icon (wrapped with standard tinting/contentColor) as the default food placeholder.
2.  **Modify `FoodDetailScreen.kt` (`FreeGluKMP/shared/src/commonMain/kotlin/com/ivan/freeglukmp/presentation/detail/FoodDetailScreen.kt`)**:
    *   Replace `AsyncImage` with `SubcomposeAsyncImage`.
    *   Add identical `loading` and `error` parameters or subcomposition blocks matching the styling of the detail header.

---

## 🧪 Verification Strategy

### 1. Backend Verification
-   Run Swift compiler & tests on the development machine with local Postgres credentials:
    ```bash
    DATABASE_USERNAME=ivan.sanchez DATABASE_PASSWORD="" cd GlutenFreeAPI && swift test
    ```
-   Ensure all tests compile, database migrations complete, and data insertion/retrieval succeeds under the new UUID format.

### 2. Frontend Verification
-   Compile and build the Kotlin Multiplatform shared module:
    ```bash
    cd FreeGluKMP && ./gradlew :shared:assemble
    ```
-   Ensure there are no compilation errors in the DTOs, Mappers, repositories, or presenters.
