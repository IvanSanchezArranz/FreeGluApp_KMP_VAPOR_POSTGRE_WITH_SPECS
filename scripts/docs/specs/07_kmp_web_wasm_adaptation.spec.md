# Spec 07: Adaptaciones para Web (Wasm) y Responsive Design

## Objetivo
Optimizar la aplicación Compose Multiplatform para asegurar su correcto funcionamiento en navegadores modernos, manejando políticas de red (CORS) y la experiencia de usuario en monitores grandes.

## Arquitectura y Componentes
1.  **Backend (Ajuste CORS en Vapor):**
    * Configurar `CORSMiddleware` en `configure.swift` para aceptar peticiones `OPTIONS`, `GET` desde orígenes de navegador (`allowedOrigin: .all` o el dominio de producción web).
2.  **Cliente UI (Responsive):**
    * Reemplazar anchos fijos. Usar `BoxWithConstraints` para cambiar la distribución de la vista si el ancho es superior a `600dp`.
    * En resoluciones de escritorio, `FoodsListScreen` debe adaptar su Grid de 2 columnas a 4, 5 o más utilizando `GridCells.Adaptive(minSize = 180.dp)`.
3.  **Gestión de Imágenes Web:**
    * Validar que Coil3 en Wasm puede solicitar las imágenes a los servidores de OpenFoodFacts (si estos bloquean CORS, proveer una capa de caché de imagen proxy en el servidor Vapor o un CDN).

## Criterios de Aceptación
- [ ] La versión Web (`./gradlew :webApp:wasmJsBrowserDevelopmentRun`) puede renderizar los datos desde el servidor localhost Vapor sin errores de bloqueos CORS en la consola del navegador.
- [ ] Al redimensionar la ventana del navegador, las tarjetas de alimentos se ajustan fluidamente, aprovechando el espacio ancho en PC y colapsando en móvil.