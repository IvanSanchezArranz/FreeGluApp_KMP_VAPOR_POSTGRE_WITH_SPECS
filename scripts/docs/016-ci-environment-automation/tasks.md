# Actionable Tasks: CI Environment Automation

This file contains the exact dependency-ordered checklists and verification commands to implement the CI Environment Automation.

---

## Phase 1: CI/CD Workflows Integration

### T1601 DEVOPS-02 Integrar reescritor de entorno en `release.yml`
*   **Descripción**: Insertar un paso de compilación previo en `.github/workflows/release.yml` que reescriba de manera dinámica el valor por defecto de `CURRENT_ENVIRONMENT` a `AppEnvironment.AUTO` antes de compilar el APK y el framework iOS.
*   **Precondiciones**: Ninguna
*   **Estimación**: 1 SP / 0.25 dev-days
*   **Owner**: @owner-devops
*   **Acceptance Criteria**:
    *   La pipeline ejecuta el paso de reescritura antes del empaquetado y compila con éxito.
*   **Test comando**: Validar sintaxis de workflow de GitHub Actions.

### T1602 DEVOPS-02 Integrar reescritor de entorno en `ci.yml`
*   **Descripción**: Insertar el mismo paso de reescritura previa en `.github/workflows/ci.yml` justo antes de construir el cliente KMP para verificar que la compilación con entorno `AUTO` pase todos los linters de integración.
*   **Precondiciones**: Ninguna
*   **Estimación**: 1 SP / 0.25 dev-days
*   **Owner**: @owner-devops
*   **Acceptance Criteria**:
    *   La pipeline de CI ejecuta el reescritor de entorno y compila con éxito.
*   **Test comando**: Validar sintaxis del fichero de CI.
