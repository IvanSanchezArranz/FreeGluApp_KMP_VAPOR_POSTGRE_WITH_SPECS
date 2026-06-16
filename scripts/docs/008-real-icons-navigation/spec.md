# Feature Specification: Real Vector Icons for Navigation

**Feature Branch**: `008-real-icons-navigation`
**Status**: Proposal

## Vision & Product Overview
Enhance the user interface and visual consistency of FreeGluApp by replacing text-based and emoji-based icons with standard, scalable Material vector images across the Compose Multiplatform UI.

## Technical Architecture

### 1. Root Cause Analysis & Problem Context
- The application's UI currently relies on hardcoded string texts (`"←"`) and emojis (`"🟢"`, `"❤️"`, `"🖤"`) to represent interactive navigation components and action buttons.
- While text emojis are functional, they do not scale perfectly with system accessibility settings, their rendering differs across operating systems (Android vs iOS vs Web), and they lack a professional appearance.
- Compose Material 3 includes standard, highly optimized vector graphics natively.

### 2. Implementation Scope
- **Bottom Navigation Bar (`App.kt`)**: 
  - Replace the text `"🟢"` with `androidx.compose.material.icons.Icons.Default.List` for the Catalog tab.
  - Replace the text `"❤️"` with `androidx.compose.material.icons.Icons.Default.Favorite` for the Favorites tab.
- **Detail Screen Top App Bar (`FoodDetailScreen.kt`)**:
  - Replace the text `"←"` with `androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack` (or `Icons.Default.ArrowBack`).
  - Replace the text `"❤️ Favorite"` and `"🖤 Add Favorite"` with standard `Icon` composables utilizing `Icons.Default.Favorite` and `Icons.Default.FavoriteBorder` (or similar unfilled/filled pairings).

## Acceptance Criteria
- [ ] Emojis are fully removed from the bottom navigation bar and replaced with `Icon` composables using Material `Icons`.
- [ ] The text-based back arrow is replaced with the standard Material back arrow icon.
- [ ] The UI renders these vectors perfectly across platforms natively via Compose Multiplatform.
