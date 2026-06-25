# Actionable Tasks: Production Cloud Deployment (Render)

This file contains the exact dependency-ordered checklists and verification commands to implement the Production Cloud Deployment feature on Render.

---

## Phase 1: Infrastructure as Code & Blueprints

### T1401 DEVOPS-02 Crear plano de infraestructura `render.yaml`
*   **Descripción**: Crear el archivo `render.yaml` en la raíz del repositorio de FreeGluApp que declare de forma declarativa la base de datos PostgreSQL (`freeglu-db`) y el servicio web `freeglu-api` de Swift Vapor (usando entorno Docker con Root Directory `GlutenFreeAPI` y variables de entorno automatizadas).
*   **Precondiciones**: Ninguna
*   **Estimación**: 2 SP / 0.5 dev-days
*   **Owner**: @owner-devops
*   **Acceptance Criteria**:
    *   La base de datos se declara correctamente en el plan gratuito.
    *   El servicio web Vapor enlaza dinámicamente `DATABASE_URL` desde la propiedad `connectionString` de la base de datos.
    *   Genera un `JWT_SECRET` aleatorio de forma segura.
*   **Test comando**: Validar estructura YAML visualmente.

---

## Phase 2: Client Config & CI/CD Integrations

### T1402 KMP-10 Configurar alternancia de red dinámico adaptativo en KMP
*   **Descripción**: Actualizar `Platform.kt` y sus implementaciones nativas en Android, iOS, JS y Wasm para añadir y respetar las constantes `USE_LOCAL_BACKEND` y `CLOUD_BACKEND_URL` al resolver `getApiBaseUrl()`.
*   **Precondiciones**: Ninguna
*   **Estimación**: 3 SP / 1 dev-day
*   **Owner**: @owner-frontend
*   **Acceptance Criteria**:
    *   Tanto el compilador nativo como el compilador JVM/JS builden sin advertencias.
    *   Los tests unitarios de KMP pasan exitosamente en local.
*   **Test comando**: `cd FreeGluKMP && ./gradlew :shared:allTests`

### T1403 DEVOPS-02 Automatizar el webhook de Render en GitHub Actions
*   **Descripción**: Añadir un paso condicional `Trigger Render Deploy` en `.github/workflows/ci.yml` que invoque mediante `curl` la URL del webhook de despliegue si se suben cambios a `main` y todos los tests pasan con éxito.
*   **Precondiciones**: Ninguna
*   **Estimación**: 2 SP / 0.5 dev-days
*   **Owner**: @owner-devops
*   **Acceptance Criteria**:
    *   La pipeline se ejecuta de manera exitosa.
    *   El comando salta de forma segura y controlada (imprimiendo un warning) si la clave secreta del webhook no está presente en los secretos del repositorio.
*   **Test comando**: Validar sintaxis de workflow de GitHub Actions.
