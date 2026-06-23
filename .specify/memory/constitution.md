<!--
=== Sync Impact Report ===
- Version change: Template -> v1.0.0
- List of modified principles:
  - PRINCIPLE_1_NAME -> I. Inicialización Síncrona de Koin (Koin Sync Lifecycle)
  - PRINCIPLE_2_NAME -> II. Enrutamiento de Red Dinámico Adaptativo (getApiBaseUrl())
  - PRINCIPLE_3_NAME -> III. Persistencia Local Offline sin Room para Wasm
  - PRINCIPLE_4_NAME -> IV. Caché y Optimización de Imágenes con Coil 3
  - PRINCIPLE_5_NAME -> V. Higiene de Git e Ignorados (/data/)
- Added sections:
  - Requisitos de Stack Tecnológico y Puertos
  - Flujo de Trabajo y Puertas de Validación
- Removed sections: None
- Templates requiring updates: None (aligned and verified)
- Follow-up TODOs: None
==========================
-->

# FreeGluApp Constitution

## Core Principles

### I. Inicialización Síncrona de Koin (Koin Sync Lifecycle)
El grafo de dependencias de Koin **NUNCA** debe inicializarse en bloqueos asíncronos o de UI de Compose (como `LaunchedEffect`). Compose realiza llamadas a `koinInject()` durante la primera composición síncrona, causando un crash fatal si el grafo no se ha cargado. Koin debe inicializarse estrictamente de forma síncrona en los puntos de entrada nativos de cada plataforma:
- **Android**: En `MainActivity.onCreate` antes de `setContent`.
- **iOS**: En `MainViewController.kt` antes de crear el `ComposeUIViewController`.
- **Web Wasm/JS**: En `main.kt` al inicio de la ejecución.

### II. Enrutamiento de Red Dinámico Adaptativo (getApiBaseUrl())
La URL base de la API debe resolverse dinámicamente mediante el patrón `expect`/`actual` de Kotlin para soportar de manera segura emuladores, simuladores y entornos de testing:
- El emulador de Android requiere conectarse a la IP virtual `10.0.2.2:8080`.
- Los simuladores de iOS, clientes Web y tests unitarios locales de JUnit (`AndroidHostTest`) requieren estrictamente `127.0.0.1:8080`.
- El servidor de Vapor o Ktor backend debe escuchar siempre en `0.0.0.0` para permitir el puenteo virtual de red de Android.

### III. Persistencia Local Offline sin Room para Wasm
Para garantizar máxima estabilidad y rendimiento, se debe evitar el uso de Room u otros ORMs inestables en entornos WebAssembly (Wasm). En su lugar, se implementará la abstracción ligera `LocalFavoritesDataSource` mediante el patrón `expect`/`actual` de Kotlin, conectada a los motores nativos clave-valor de cada entorno:
- **Android**: Respaldado por `SharedPreferences`.
- **iOS**: Respaldado por `NSUserDefaults`.
- **Web Wasm/JS**: Respaldado por `localStorage` síncrono del navegador.

### IV. Caché y Optimización de Imágenes con Coil 3
Las listas y colecciones densas de productos con imágenes deben optimizarse para prevenir latencia, consumo excesivo de red y sobrecarga de memoria. En `App.kt`, se debe configurar un `ImageLoader` de Coil 3 personalizado, asignando de manera obligatoria el **25% de la memoria RAM del sistema** (`maxSizePercent(context, 0.25)`) para el almacenamiento en caché de mapas de bits decodificados.

### V. Higiene de Git e Ignorados (/data/)
El archivo `.gitignore` raíz debe bloquear estrictamente la carpeta de datos masivos de la raíz sin afectar a los paquetes lógicos de Kotlin. Se debe utilizar la barra diagonal de raíz (`/data/`) para indicar que se ignore únicamente el dataset de 12 GB, evitando ignorar de forma recursiva paquetes o directorios que contengan la subcarpeta `data` (por ejemplo, `com.ivan.freeglukmp.data/...`).

## Requisitos de Stack Tecnológico y Puertos

El ecosistema FreeGluApp se compone de los siguientes elementos que deben mantener compatibilidad de puertos e interfaces:
1. **Backend API**: Swift Vapor 4 (Swift 6) o el clon de réplica en Kotlin Ktor. Ambos deben exponer la API REST asíncrona en el puerto `8080` (enlace en `0.0.0.0` para admitir emuladores).
2. **Cliente Multiplataforma**: Kotlin Multiplatform con interfaz de usuario declarativa en Compose Multiplatform y diseño Material 3.
3. **Pipeline ETL**: Scripts en Python 3 (Pandas + SQLAlchemy) dedicados a la extracción, limpieza e ingesta del gran volumen de datos desde archivos locales hacia PostgreSQL en puerto `5432`.

## Flujo de Trabajo y Puertas de Validación

Cada desarrollo en el repositorio debe seguir rigurosamente los siguientes controles de calidad antes de considerarse finalizado:
1. **Fase de Investigación y Planificación**: Documentar la estrategia de cambio respetando los planos de Spec-Kit en `scripts/docs/`.
2. **Higiene de Código**: Mantener tipos estrictos, evitar de manera absoluta dependencias huérfanas o advertencias del compilador. No se permiten supresiones de advertencias (warnings) ni bypass de tipos.
3. **Fase de Validación**:
   - Para cambios en el shared de KMP: `./gradlew :shared:allTests` debe pasar de forma exitosa en el 100% de los casos.
   - Para cambios en Backend: Ejecutar suite de pruebas de Vapor (`swift test`) o de Ktor (`./gradlew test`).
   - El pipeline CI/CD en `.github/workflows/ci.yml` ejecutará de forma automática estas validaciones en entornos macOS con PostgreSQL en Docker.

## Governance
La Constitución es el documento supremo del repositorio y supersede cualquier otra práctica informal de desarrollo. Cualquier enmienda o ampliación de estos principios fundamentales requiere:
1. Documentar los motivos en un nuevo plano de especificación en `scripts/docs/`.
2. Actualizar el archivo `GEMINI.md` para notificar al equipo de desarrollo y agentes de IA.
3. Incrementar la versión semántica de esta Constitución y registrar la fecha de última actualización.

**Version**: 1.0.0 | **Ratified**: 2026-06-19 | **Last Amended**: 2026-06-23
