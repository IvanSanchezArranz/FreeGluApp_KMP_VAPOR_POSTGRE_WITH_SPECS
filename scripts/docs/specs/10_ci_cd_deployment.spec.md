# Spec 10: Integración y Despliegue Continuo (CI/CD)

## Objetivo
Automatizar la compilación, las pruebas y el despliegue de los distintos artefactos (Backend, Web, Android e iOS) utilizando GitHub Actions (o similar).

## Arquitectura y Componentes
1.  **Backend Deployment (Docker):**
    * *Nota: Ya existe un `Dockerfile` y `docker-compose.yml` en la carpeta `backend/`.*
    * Crear un workflow `.github/workflows/deploy-backend.yml` que: compile la imagen Docker de Vapor y la suba a un registro (ej. Docker Hub o GitHub Container Registry).
2.  **Web Deployment (Wasm):**
    * Crear un workflow que ejecute `./gradlew :webApp:wasmJsBrowserDistribution`.
    * Desplegar la carpeta generada (los estáticos y el `.wasm`) en GitHub Pages, Vercel o Netlify.
3.  **Mobile Build:**
    * Workflow que compile el `.apk` de Android (`./gradlew :androidApp:assembleRelease`).
    * *(Opcional/Avanzado)*: Workflow en macOS runner para compilar el framework de iOS y asegurar que no hay errores de compilación en Xcode.

## Criterios de Aceptación
- [x] Cada vez que se hace un `git push` a la rama `main`, se ejecutan los tests automáticamente.
- [x] Si los tests pasan, se genera una nueva versión de la app web y se actualiza el backend.