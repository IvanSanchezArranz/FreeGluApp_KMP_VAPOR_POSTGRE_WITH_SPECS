# Implementation Plan: Production Bug Fixes

**Branch**: `006-production-bug-fixes`

## Summary
Align ID types between the database (BIGINT) and Vapor (Int) along with KMP (Int? in DTO, String in Model), and resolve Koin's Compose context binding error.

## Project Structure (Target Files)
```text
FreeGluApp/
├── GlutenFreeAPI/Sources/GlutenFreeAPI/Models/Food.swift
├── GlutenFreeAPI/Tests/GlutenFreeAPITests/GlutenFreeAPITests.swift
├── FreeGluKMP/shared/src/commonMain/kotlin/com/ivan/freeglukmp/App.kt
├── FreeGluKMP/shared/src/commonMain/kotlin/com/ivan/freeglukmp/data/remote/DTOs.kt
└── FreeGluKMP/shared/src/commonMain/kotlin/com/ivan/freeglukmp/data/remote/Mappers.kt
```

## Implementation Steps
1. Create Spec-Kit documentation files for Spec 006.
2. Edit `FreeGluKMP/shared/src/commonMain/kotlin/com/ivan/freeglukmp/App.kt`:
   - Import `org.koin.compose.KoinContext`.
   - Wrap the App layout hierarchy with `KoinContext { ... }`.
3. Edit `GlutenFreeAPI/Sources/GlutenFreeAPI/Models/Food.swift`:
   - Change ID property from `UUID?` to `@ID(custom: .id, generatedBy: .database) var id: Int?`.
   - Update initializer signature and body.
4. Edit `GlutenFreeAPI/Tests/GlutenFreeAPITests/GlutenFreeAPITests.swift`:
   - Change `invalidId` from `UUID()` to a random Int (e.g. `999999`).
5. Edit `FreeGluKMP/shared/src/commonMain/kotlin/com/ivan/freeglukmp/data/remote/DTOs.kt`:
   - Change `id` in `FoodDTO` from `String?` to `Int?`.
6. Edit `FreeGluKMP/shared/src/commonMain/kotlin/com/ivan/freeglukmp/data/remote/Mappers.kt`:
   - Map `id = this.id?.toString() ?: ""`.
7. Verify all changes locally by building.
