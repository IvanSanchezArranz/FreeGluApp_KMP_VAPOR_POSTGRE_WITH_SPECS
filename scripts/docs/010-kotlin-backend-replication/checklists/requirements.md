# Requirements Checklist: Kotlin Ktor Backend Replication

This checklist is used to verify that the planned Kotlin backend matches all architectural, functional, and non-functional requirements of the `GlutenFreeAPI` project before execution begins.

---

## 📋 1. Project Compliance Checklist

- [x] **Platform Target Compliance** — Plan targets JVM 21, matching high-performance container standards (TC-01).
- [x] **Framework Compliance** — Ktor Server Netty selected for high-efficiency asynchronous requests (TC-02).
- [x] **Database Isolation & Integrity** — Plan preserves exact column types, constraints, and indices matching Vapor Fluent's setup (TC-03, TC-04).
- [x] **CORS Configuration Parity** — Allowed headers, origins, and methods match the Vapor config exactly (TC-06).
- [x] **JSON Payload Parity** — Identical camelCase output field naming to ensure zero-effort client switching.
- [x] **Zero Vibe-Coding Compliance** — Spec defined using forward-chained constraints with explicit metrics, out-of-scope bounds, and automated testing command pipelines.

---

## 🛠️ 2. Functional Parity Mapping

| Requirement / Endpoint | Vapor Swift Mapping | Proposed Kotlin Ktor Mapping | Status |
| :--- | :--- | :--- | :--- |
| **GET /** | Text status response | `GET /` returning text payload | ✅ Planned |
| **GET /foods** | Paginated output via Fluent Page | `GET /foods` paginated using Exposed offset/limit query | ✅ Planned |
| **GET /foods/search?q=** | Paginated filtered query | `GET /foods/search` with case-insensitive `ILIKE` mapping | ✅ Planned |
| **GET /foods/:id** | Fetch Food by standard UUID | `GET /foods/{id}` with UUID parsing and 404 handler | ✅ Planned |
| **CORS Middleware** | Vapor CORSMiddleware | Ktor CORS plugin with identical parameters | ✅ Planned |
| **Database Credentials** | Environment-based config | Environment-based config via Application.conf / System | ✅ Planned |

---

## 🧪 3. Quality & Security Assurance

- [ ] **SQL Injection Prevention** — Ensure all queries use Exposed ORM's DSL or prepared parameter bindings.
- [ ] **UUID Validation** — Route parameter validation for `foodID` to reject arbitrary non-UUID strings early (TC-04).
- [ ] **Automated Testing Coverage** — Integration test suite must cover both success and negative routes (such as 400 Bad Request and 404 Not Found), aiming for >= 80% coverage (T109).
