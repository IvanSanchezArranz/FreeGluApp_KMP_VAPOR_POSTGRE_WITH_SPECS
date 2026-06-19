# Requirements Checklist: App Headers Standardization

This checklist is used to verify that the planned app header and grid standardization matches all technical, functional, and non-functional specifications.

---

## 📋 1. UI Parity & Design Compliance

- [x] **Visual Parity** — Top-level list screens (Catalog and Favorites) share identical left-aligned header layout and weight.
- [x] **Grid Parity** — Both grids use `GridCells.Fixed(2)` with `12.dp` margins, ensuring standard cards.
- [x] **Contrast Compliance** — Deep emerald text colors map to white/mint-white surfaces for perfect WCAG 2.1 AA legibility.
- [x] **Touch Target Compliance** — Back buttons and favorite icons maintain comfortable touch targets (minimum `48.dp`).

---

## 🛠️ 2. Architectural Cleanliness

- [x] **Material 3 Integration** — Use native Compose Multiplatform layout structures (`Row`, `Column`, `Grid`) without deprecated hacks.
- [x] **No Hardcoded Screen Sizes** — Adapt safely to both landscape/tablet widths via symmetrical cell constraints.
- [x] **Safe Koin & State Contexts** — Preserve existing dependency injection and flow emission chains during refactoring.
