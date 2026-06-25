# Feature Specification: CI Environment Automation

**Feature Branch**: `016-ci-environment-automation`
**Status**: Draft (Under Review)

## Vision & Product Overview
To ensure seamless deployments, the KMP client app should default to `AppEnvironment.LOCAL` during local development on physical debug devices and emulators. However, when compiled on GitHub Actions (CI/CD) for release, the build must automatically target `AppEnvironment.AUTO` so that the generated binary dynamically routes network traffic to the live Render cloud backend on physical devices. This specification defines a fully automated, cross-platform pre-build rewriting step within the CI/CD pipelines to achieve this zero-touch environment switching.

---

## Technical Scope

### 1. Pre-Build Code Rewriting
- Integrate a Python-based inline pre-build rewriting script in the GitHub Actions runners.
- The script searches for `val CURRENT_ENVIRONMENT = AppEnvironment.LOCAL` inside `FreeGluKMP/shared/src/commonMain/kotlin/com/ivan/freeglukmp/Platform.kt` and replaces it with `val CURRENT_ENVIRONMENT = AppEnvironment.AUTO` right before Gradle compilation starts.

### 2. Symmetrical Support
- Apply this automation step to:
  - `.github/workflows/ci.yml` (For continuous integration and Web Wasm compilation verification).
  - `.github/workflows/release.yml` (For Android Release APK and iOS Framework compilations).

---

## Criterios de Aceptación
- [ ] La pipeline de GitHub Actions se ejecuta con éxito y realiza el reemplazo de entorno antes de compilar.
- [ ] Los binarios generados en el CI (`.apk` y frameworks) se compilan con el entorno configurado como `AUTO`.
- [ ] El código fuente local en la máquina del desarrollador no se ve modificado por la ejecución del CI (el cambio solo ocurre en el runner efímero).
