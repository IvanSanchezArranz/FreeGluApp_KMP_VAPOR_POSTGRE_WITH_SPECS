# Requirements Checklist: Product CRUD Management

This checklist is used to verify that the planned Create, Update, and Delete actions for products match all technical, functional, and non-functional specifications.

---

## 📋 1. UI Parity & Design Compliance

- [ ] **Symmetrical Form Grid** — Form fields (Code, Name, Brand, etc.) must be aligned beautifully according to the project's Material 3 standard.
- [ ] **Touch Target Compliance** — Save, edit, and delete action buttons maintain comfortable touch targets (minimum `48.dp`).
- [ ] **Feedback States** — Screen forms display clear visual loading indicators during API submission, and error messages for invalid inputs.
- [ ] **Security Visibility** — Create, edit, and delete operations are strictly hidden when the user is logged out, ensuring simple read-only compliance.

---

## 🛠️ 2. Architectural Cleanliness

- [ ] **Clean Data Transitions** — The UI only interacts with `FoodModel` domain classes; Ktor network structures are strictly mapped from `FoodRequestDTO` inside `FoodRepositoryImpl`.
- [ ] **UDF Compliance** — ViewModels expose a unidirectional flow of state, and Compose views only observe and trigger corresponding events.
- [ ] **Authenticated API Requests** — All POST, PUT, and DELETE write calls safely attach the bearer token, returning 401 on missing auth.
- [ ] **Reactive Auto-Logout** — Capturing 401 Unauthorized automatically calls auth logout and redirects the user to the Login screen.
- [ ] **KMP Multiplatform Compiles** — Shared structures compile successfully on Android, iOS, and WebAssembly targets.
