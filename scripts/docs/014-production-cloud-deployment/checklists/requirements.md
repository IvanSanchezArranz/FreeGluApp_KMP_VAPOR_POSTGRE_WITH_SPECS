# Checklist: Production Cloud Deployment (Render)

## 1. Sanidad de Infraestructura (Render Blueprint)
- [ ] El archivo `render.yaml` está ubicado exactamente en la raíz del repositorio.
- [ ] La base de datos PostgreSQL se declara utilizando el plan gratuito (`plan: free`).
- [ ] El servicio web `freeglu-api` utiliza el plan gratuito (`plan: free`), entorno Docker (`env: docker`) y apunta a la subcarpeta `GlutenFreeAPI` (`rootDir: GlutenFreeAPI`).
- [ ] La variable `DATABASE_URL` se enlaza correctamente de forma interna utilizando `fromDatabase: connectionString` para evitar exponer credenciales.
- [ ] Se incluye la generación automática y segura de `JWT_SECRET`.

## 2. Configuración de Red Adaptativa (KMP)
- [ ] Las constantes `USE_LOCAL_BACKEND` y `CLOUD_BACKEND_URL` están unificadas en `Platform.kt` en `commonMain`.
- [ ] La resolución de URL en Android (`Platform.android.kt`) respeta tanto el toggle de nube como los mapeos de emuladores y tests junit locales.
- [ ] Los objetivos nativos e iOS (`Platform.ios.kt`) resuelven de forma limpia.
- [ ] Las plataformas web (`Platform.js.kt` y `Platform.wasmJs.kt`) compilan con éxito sin advertencias de tipos.

## 3. Seguridad y CI/CD
- [ ] El paso `Trigger Render Deploy` en `.github/workflows/ci.yml` está correctamente limitado al evento push en la rama `main`.
- [ ] El script utiliza una comprobación condicional segura (`if [ -n "$RENDER_DEPLOY_HOOK" ]; then ...`) para evitar fallos de ejecución si no se han configurado los secretos en GitHub.
- [ ] El secreto del deploy hook se inyecta desde `${{ secrets.RENDER_DEPLOY_HOOK_URL }}` de forma segura y sin dejar logs.
