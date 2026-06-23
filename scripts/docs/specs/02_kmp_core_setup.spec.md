# Spec 02: Configuración Core de Kotlin Multiplatform

## Objetivo
Preparar el esqueleto base del proyecto cliente (basado en el template FreeGluKMP) para soportar la arquitectura de inyección de dependencias, consumo de red, gestión de asincronía y carga de imágenes para iOS, Android y Web (Wasm/JS).

## Archivo objetivo
`FreeGluKMP/gradle/libs.versions.toml`

## Arquitectura y Componentes
1.  **Gestión de Dependencias (Version Catalog - `libs.versions.toml`):**
    * **Ktor (`3.x.x`):** Cliente HTTP multiplataforma (`ktor-client-core`, `ktor-client-content-negotiation`, cliente OkHttp para Android, Darwin para iOS).
    * **Koin (`3.5.x`):** Service Locator / Inyección de dependencias (`koin-core`, `koin-compose-viewmodel`).
    * **Kotlinx Serialization:** Para parseo de JSON.
    * **Coil3 (`3.x.x`):** Carga de imágenes por red nativa para Compose Multiplatform.
    * **Paging3:** Para manejar las páginas infinitas.
2.  **Configuración de Módulos:**
    * Inclusión de todas estas dependencias en el bloque `sourceSets.commonMain.dependencies` de `shared/build.gradle.kts`.

## Criterios de Aceptación
- [x] La sincronización de Gradle finaliza sin conflictos de resolución de versiones.
- [x] La app compila nativamente en Android (APK) e iOS (vía xcode-build / framework embed).
- [x] La app compila para entorno Web (Wasm) sin errores de vinculación.

---

## Notas de Configuración de Android

### Permisos de Red en `AndroidManifest.xml`

Para que la aplicación Android pueda realizar llamadas de red y conectarse al backend, es **obligatorio** configurar su manifiesto (`androidApp/src/main/AndroidManifest.xml`).

1.  **Permiso de Internet:** Se debe añadir el permiso para acceder a internet.
    ```xml
    <uses-permission android:name="android.permission.INTERNET" />
    ```

2.  **Permitir Tráfico HTTP (para Desarrollo):** Para poder conectarse a un servidor local (ej: `http://127.0.0.1:8080`) durante el desarrollo, se debe permitir el tráfico de texto no cifrado (no HTTPS).
    ```xml
    <application
        ...
        android:usesCleartextTraffic="true">
        ...
    </application>
    ```

Sin estos dos ajustes, cualquier intento de conexión de red desde la aplicación Android fallará.

---

## Notas sobre la Inyección de ViewModels con Koin

La inyección de ViewModels en un proyecto KMP con Compose presenta varios desafíos de configuración que pueden llevar a errores de `IrLinkageError` en iOS o `Could not resolve dependency` en Gradle.

### Solución: Inicialización Pre-Composición de Koin (Detección de Error Crítico)
Inicialmente se intentó arrancar Koin dentro de un bloque `LaunchedEffect` en la UI común, pero esto causaba un crash fatal: `IllegalStateException: KoinApplication has not been started`, debido a que la primera composición de Compose invoca los inyectores de dependencia (`koinInject()`) antes de que se dispare el `LaunchedEffect`.

La **solución senior definitiva** consiste en arrancar Koin de manera global y síncrona en los **puntos de entrada nativos** de cada plataforma *antes* de que empiece la composición de Compose:
*   **Android (`MainActivity`):** Invocando `initKoin()` en el `onCreate` nativo previo a `setContent { App() }`.
*   **iOS (`MainViewController`):** Invocando `initKoin()` antes de retornar `ComposeUIViewController { App() }`.
*   **Web (`main.kt`):** Invocando `initKoin()` previo a la inicialización de `ComposeViewport`.

---

## Notas sobre la Conexión al Backend Local

### Problema: `Failed to connect to localhost` en el Emulador de Android y Colisión en Tests Locales

Cuando se ejecuta la aplicación en un emulador de Android, la dirección `localhost` o `127.0.0.1` se refiere al propio emulador, no a la máquina anfitriona (tu ordenador) donde se está ejecutando el servidor backend. Esto provoca un error de conexión. Sin embargo, si ponemos `10.0.2.2` de forma fija, los tests unitarios locales que corren sobre la JVM de la computadora (test de Android Host) fallarán porque la máquina de desarrollo sí necesita `127.0.0.1`.

### Solución: URL Base por Plataforma con `expect`/`actual` e Inteligencia Anti-Nulos de Tests
Implementamos una abstracción `getApiBaseUrl()` con detección inteligente de entorno de ejecución en Android (`Platform.android.kt`):
```kotlin
actual fun getApiBaseUrl(): String {
    val fingerprint = Build.FINGERPRINT
    // Detecta si es un test unitario en el host o el emulador (fingerprint nulo o Robolectric)
    val isUnitTest = fingerprint == null || fingerprint == "unknown" || fingerprint.startsWith("generic") || Build.DEVICE == null || Build.BRAND == "robolectric"
    return if (isUnitTest) {
        "http://127.0.0.1:8080" // Mac Host (Tests unitarios)
    } else {
        "http://10.0.2.2:8080"  // Emulador de Android
    }
}
```
Esto resuelve de forma transparente y automatizada las peticiones de red en tiempo de ejecución tanto para emuladores activos como para runners de integración continua locales.

---

## Notas sobre Rendimiento de Imágenes (Coil 3 Cache)
Para garantizar una tasa de cuadros fluida y evitar latencia en la carga de red de imágenes recurrentes, configuramos una política intensiva de caché de memoria en `App.kt` aprovechando Coil 3:
```kotlin
setSingletonImageLoaderFactory { context ->
    ImageLoader.Builder(context)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .memoryCache {
            MemoryCache.Builder()
                .maxSizePercent(context, 0.25) // Asigna el 25% de la RAM disponible para bitmaps
                .strongReferencesEnabled(true)
                .build()
        }
        .build()
}
```
        // ...
    }
    ```
