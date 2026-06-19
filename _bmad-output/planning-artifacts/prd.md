---
title: "Product Requirements Document: Kotlin Backend Replicated Service"
version: 1.0.0
date: 2026-06-19
status: Approved
author: "Gemini CLI Agent"
llm_directives:
  - "Strictly adhere to the Technical Constraints (Section 2)."
  - "Do not implement features listed in the Out of Scope section (Section 6)."
  - "Ensure exact parity of JSON payload schemas and query parameters with the Swift Vapor backend."
---

# Product Requirements Document (PRD): GlutenFree Kotlin Backend

## 1. Overview & Goals
*   **Executive Summary**: Replicate the entire functionality of the Swift Vapor-based `GlutenFreeAPI` in a new Kotlin-based backend project. This ensures we have a dual-backend architecture where both implementations are interchangeable for the KMP client.
*   **Problem Statement**: While Swift Vapor is lightweight, having a secondary backend in Kotlin (Ktor) increases platform robustness, enables developer flexibility, and provides a unified Kotlin environment across client and server.
*   **Goals**:
    *   Expose identical REST API endpoints.
    *   Connect to the same PostgreSQL database schema.
    *   Support Fluent-compatible pagination query parameters (`page` and `per`) and output format.
    *   Maintain absolute database constraint and indexing parity.
*   **Non-Goals**:
    *   We are not migrating away from Vapor; we are building a parallel, interchangeable service.
    *   No UI or web portal will be developed for this backend.

---

## 2. Technical Constraints (TC)

*   **TC-01**: **Language & Runtime**: Must be built using **Kotlin JVM (target Java 21)** and **Kotlin 2.x**.
*   **TC-02**: **Framework**: Must use **Ktor Server** (asynchronous, coroutine-based) as the routing and application engine.
*   **TC-03**: **Database Connection**: Must connect to PostgreSQL using **HikariCP** and **Exposed ORM** (or native SQL/R2DBC) for database operations.
*   **TC-04**: **API Contracts**: All JSON response schemas and HTTP status codes must be identical to the Swift Vapor service.
*   **TC-05**: **Pagination Parity**: Must support standard page requests with `page` (1-based index) and `per` (items per page) parameters.
*   **TC-06**: **CORS Support**: Must configure CORS to allow connections from all origins, identical to Vapor's middleware configuration.

---

## 3. Users & Personas

*   **Persona 1**: *Client Developer (KMP)*
    *   *Characteristics*: Works on Compose Multiplatform frontend.
    *   *Pain point*: Unstable server behaviors or divergent APIs.
    *   *Goal*: Switch the base URL in KMP to point to either Swift Vapor or Kotlin Ktor, and have the client work flawlessly without changing client code.

### 🤖 Core Orchestration: BMAD Agents
This project's lifecycle, from requirements through implementation, is driven by the six specialized agents defined in the [BMAD Agents Specification](agents.md):
*   📊 **Mary, Business Analyst**: Orchestrates the initial discovery.
*   📚 **Paige, Technical Writer**: Documents and validates specifications and diagrams.
*   📋 **John, Product Manager**: Maintains the PRD and epics.
*   🎨 **Sally, UX Designer**: Designs interactions and responsive states.
*   🏗️ **Winston, System Architect**: Secures data/API schemas and structural parity.
*   💻 **Amelia, Senior Engineer**: Executes tasks, runs reviews, and conducts testing.

---

## 4. Functional Requirements (User Stories)

### US-01: Health Endpoint Check
*   **As a** system monitor or client app,
*   **I want to** hit the root URL of the service,
*   **So that** I can verify the API is up and running.
*   **Acceptance Criteria**:
    *   **AC-01-A**: `GET /` MUST return HTTP 200 OK.
    *   **AC-01-B**: Response body MUST be a plain text string: `API Gluten Free funcionando 🚀` (with identical emojis and text).

### US-02: Paginated Food Catalog Retrieval
*   **As a** client application,
*   **I want to** retrieve a list of foods with pagination,
*   **So that** I can render an infinite-scroll product grid without overloading the network.
*   **Acceptance Criteria**:
    *   **AC-02-A**: `GET /foods` MUST return HTTP 200 OK.
    *   **AC-02-B**: Response payload MUST match the standard Vapor Page JSON structure:
        ```json
        {
          "items": [
            {
              "id": "UUID-string",
              "code": "string",
              "name": "string",
              "brand": "string?",
              "categories": "string?",
              "ingredients": "string?",
              "imageUrl": "string?",
              "countries": "string?",
              "glutenFree": true,
              "createdAt": "date-string?"
            }
          ],
          "metadata": {
            "page": 1,
            "per": 20,
            "total": 150
          }
        }
        ```
    *   **AC-02-C**: Query parameters `page` and `per` MUST default to `1` and `20` respectively if missing.

### US-03: Insensitive Case-Insensitive Paginated Search
*   **As a** client application,
*   **I want to** search for foods by name, brand, or categories,
*   **So that** users can find specific gluten-free products matching their keywords.
*   **Acceptance Criteria**:
    *   **AC-03-A**: `GET /foods/search?q={term}` MUST return HTTP 200 OK.
    *   **AC-03-B**: If the query parameter `q` is missing or empty, MUST return HTTP 400 Bad Request with a message.
    *   **AC-03-C**: Search MUST match `name`, `categories`, or `brand` case-insensitively using `ILIKE`.
    *   **AC-03-D**: Search results MUST be paginated matching the structure of `GET /foods`.

### US-04: Retrieve Single Food by ID
*   **As a** client application,
*   **I want to** fetch a single food product by its unique identifier,
*   **So that** I can render its complete detail view.
*   **Acceptance Criteria**:
    *   **AC-04-A**: `GET /foods/{id}` MUST return HTTP 200 OK with the full food JSON object.
    *   **AC-04-B**: If the food ID is not found, MUST return HTTP 404 Not Found.
    *   **AC-04-C**: Food ID parameter MUST be validated as a standard UUID format.

---

## 5. Non-Functional Requirements (NFR)

*   **NFR-01 (Security)**: Safe SQL bindings: SQL query parameters must be securely bound to prevent SQL Injection (mandatory compliance with Principle I).
*   **NFR-02 (Performance)**: High-performance asynchronous non-blocking handling using Ktor's Netty engine and Kotlin Coroutines.
*   **NFR-03 (Observability)**: Standard logging of requests and exceptions.

---

## 6. Out of Scope (OOS)

*   **OOS-01**: Admin dashboard or Web user interfaces.
*   **OOS-02**: Complex user login/JWT integration (reserved for a later dedicated auth sprint, matching current Vapor state).
*   **OOS-03**: No direct data ingestion pipelines inside the Ktor app (handled externally via python ETL script).

---

## 7. Success Metrics

| Metric ID | Metric Name | Baseline | Target | Owner |
| :--- | :--- | :--- | :--- | :--- |
| SM-01 | API Contract Parity | 0% | 100% | Architect / QA |
| SM-02 | KMP Client Compatibility | 0% | 100% | Frontend Dev |
| SM-03 | Build & Launch Automation | 0% | 100% | DevOps |
