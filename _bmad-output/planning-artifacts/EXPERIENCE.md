---
title: "Experience Specifications: Accessible Organic Navigation"
version: 1.1.0
date: 2026-06-19
status: approved
author: "Sally, UX Designer"
---

# User Experience & Interaction Specification (EXPERIENCE.md)

## 1. Foundation
- **Target Platform**: Kotlin Multiplatform with Compose Multiplatform.
- **Aesthetic Movement**: *Warm Minimalist Bento*.
- **Reference**: Follows visual styling tokens specified in `{tokens.colors}` within `DESIGN.md`.

---

## 2. Information Architecture & Navigation
- **Hub-and-Spoke Flow**:
  - **Hub (FoodsListScreen)**: Full catalog search, bento-grid cards, filter pills.
  - **Spoke (FoodDetailScreen)**: Seamless card expansion/transition, large organic hero header, sticky scroll sections for Ingredients and Certifications, and a persistent "Add to Favorites" floating pill.
  - **Favorites Tab (FavoritesScreen)**: Clean, high-density listing of personal saved foods.

---

## 3. Interaction & Animation Patterns
- **Active Click Feedback**: All interactive cards scale down slightly on press (`0.98` scale transition) to feel highly tactile.
- **Image Load Transition**: Smooth crossfade transition on Coil image retrieval.
- **Toggle Favorited State**: Gentle scale pop animation (spring effect) on the heart icon when selected.

---

## 4. State Patterns (UX Feedback States)
- **Loading State**: Subtle, organic shimmer effect on the bento cards while retrieving paginated foods.
- **Error/Placeholder State**: If an item does not contain a valid image URL, display a stylized placeholder Card with a warm sage icon.
- **Empty Search State**: Render a clean, illustrated organic graphic with helpful search suggestions (e.g. "Try searching for 'Bread' or 'Pasta'") instead of an empty white void.

---

## 5. Accessibility Floor (WCAG 2.1 AA)
- **Minimum Contrast**: Every text element has a minimum contrast ratio of 4.5:1. Primary headings have >12:1 contrast.
- **Touch Target Density**: Every interactive button, pill, or card has a minimum touch target size of **48.dp x 48.dp** to support easy thumb navigation.
- **Color Independence**: Gluten-free status is indicated both by the primary accent color (soft warm gold/green badge) and by clear, readable text or a distinct checkmark icon, ensuring color-blind users can instantly determine safety.
