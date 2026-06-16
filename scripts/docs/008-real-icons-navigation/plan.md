# Implementation Plan: Real Vector Icons for Navigation

**Branch**: `008-real-icons-navigation`

## Summary
Replace the temporary emoji and text-based icons in the application's navigation components with standard `androidx.compose.material.icons.Icons` for a more professional, accessible, and native look across all platforms.

## Project Structure (Target Files)
```text
FreeGluApp/
├── FreeGluKMP/shared/src/commonMain/kotlin/com/ivan/freeglukmp/App.kt
├── FreeGluKMP/shared/src/commonMain/kotlin/com/ivan/freeglukmp/presentation/detail/FoodDetailScreen.kt
└── scripts/docs/008-real-icons-navigation/
    ├── spec.md
    ├── plan.md
    ├── tasks.md
    └── checklists/requirements.md
```

## Implementation Steps
1. Create the Spec 008 documentation structure.
2. Edit `App.kt`:
   - Import `androidx.compose.material.icons.Icons` and the required `filled` icon vectors (`List`, `Favorite`).
   - Import `androidx.compose.material3.Icon`.
   - Update the `NavigationBarItem` icon lambda parameters to use the new `Icon` composables instead of `Text`.
3. Edit `FoodDetailScreen.kt`:
   - Import standard Material icons (`ArrowBack`, `Favorite`, `FavoriteBorder`).
   - Import the `Icon` composable.
   - Replace the `Text("←")` in the `navigationIcon` with `Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")`.
   - Update the action button logic to display an `Icon` and `Text` pairing alongside each other instead of a raw emoji text.
4. Verify the changes locally by compiling the KMP shared module.
