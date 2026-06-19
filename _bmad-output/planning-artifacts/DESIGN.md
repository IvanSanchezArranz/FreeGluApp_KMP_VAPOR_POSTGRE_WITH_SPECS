---
title: "Design Specifications: Premium Natural Visual Identity"
version: 1.1.0
date: 2026-06-19
status: approved
author: "Sally, UX Designer"
tokens:
  colors:
    light:
      primary: "0xFF1B3D2F" # Deep Forest Green (accessible high-contrast, premium)
      secondary: "0xFF5D7A68" # Warm Sage Green (subtle, elegant)
      accent: "0xFFD4AF37" # Soft Warm Gold (organic, certified feeling)
      background: "0xFFF7F5F0" # Warm Sand Cream (reduces eye strain, organic vibe)
      surface: "0xFFFFFFFF" # Pure White (crisp floating layers)
      onPrimary: "0xFFFFFFFF"
      onBackground: "0xFF12231A" # Near Black Forest (excellent legibility)
      onSurface: "0xFF12231A"
    dark:
      primary: "0xFF8FBC8F" # Soft Mint Sage
      secondary: "0xFF5D7A68"
      accent: "0xFFF3C63F"
      background: "0xFF111714" # Deep Obsidian Charcoal-Green
      surface: "0xFF1B2420" # Dark Forest Slate
      onPrimary: "0xFF111714"
      onBackground: "0xFFE8F0EA"
      onSurface: "0xFFE8F0EA"
  typography:
    headlineLarge: "Editorial Serifs or Elegant Sans, bold, tracking wide"
    bodyLarge: "Readable Sans, medium weight, high-contrast"
  shapes:
    card: "20.dp Rounded Corners (Bento Grid style)"
    button: "50.dp Rounded Corners (Pill shape for modern touch)"
---

# Visual Identity Specification (DESIGN.md)

## 1. Brand & Style
*   **Philosophy**: *Premium Organic & Natural*.
    *   Gluten-free products should feel natural, high-end, and extremely safe to consume.
    *   We move away from high-contrast flat green primaries and muddy green backgrounds. Instead, we embrace a **Warm Sand Cream** background combined with a **Deep Forest Green** primary, offset by subtle **Sage** accents and **Soft Warm Gold** highlights for certification badges.
    *   The user interface adopts a modern, minimal editorial aesthetic with spacious padding, thin elegant borders, and soft shadows (glassmorphism/bento grid panels).

---

## 2. Colors & Accessibility (WCAG 2.1 AA)

### Light Mode Color Hierarchy:
- **Primary (`0xFF1B3D2F`)**: Used for headings, buttons, and key navigation elements. Has an outstanding contrast ratio of **12.5:1** against the Warm Sand Cream background, exceeding WCAG AAA standard.
- **Secondary (`0xFF5D7A68`)**: Used for secondary text, labels, and borders.
- **Accent (`0xFFD4AF37`)**: Used for gluten-free checkmarks, active status tags, and certified badges.
- **Background (`0xFFF7F5F0`)**: A beautiful, warm sand cream that feels soft on the eyes.
- **Surface (`0xFFFFFFFF`)**: White cards floating with an ultra-soft drop shadow and a thin `0.5.dp` solid sage border with `0.1` opacity.

---

## 3. Typography & Hierarchy
*   **Display / Title**: Spacious, bold, editorial headers.
*   **Body Text**: Highly legible sans-serif with comfortable letter-spacing.
*   **Labels / Badges**: Caps or medium weight with positive tracking.

---

## 4. Components Layout & Spacing
- **FoodCard**:
  - Rounded corners: `20.dp` (Bento style).
  - Background: Floating white surface (`0xFFFFFFFF`).
  - Border: Subtle outline (`1.dp` thickness, `Color(0x115D7A68)` Sage opacity).
  - Shadow: Soft blur shadow (`elevation = 2.dp`).
  - Image: Integrated `SubcomposeAsyncImage` with a clean shimmer loading and a beautiful sage icon fallback.
- **Search Bar**:
  - Capsule pill shape (`28.dp` or `50.dp` round).
  - Warm surface background (`0xFFFFFFFF`) with an active sage border.
