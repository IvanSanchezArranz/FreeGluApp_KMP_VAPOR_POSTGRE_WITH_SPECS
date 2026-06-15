# FreeGluApp 🚀
### Monorepo Multiplataforma para Catálogo Alimenticio Gluten Free

Bienvenido a **FreeGluApp**, una solución integral, de alto rendimiento y robusta diseñada para conectar a los usuarios con un catálogo masivo de alimentos libres de gluten (Gluten Free). Este proyecto se compone de una API Backend construida en **Swift Vapor**, un cliente multiplataforma construido en **Kotlin Multiplatform (KMP) con Compose** (compatible con Android, iOS y Web Wasm/JS), un pipeline de datos ETL en **Python**, y un flujo integrado de integración continua (CI/CD).

La creación y maduración de este repositorio se realizó de forma **100% autónoma** bajo la metodología **Spec-Kit**, traduciendo especificaciones vivas directas en código de producción optimizado de nivel senior.

---

## 🛠️ Tecnologías y Stack Tecnológico

### Backend (Swift Vapor API)
*   **Vapor 4:** Framework web asíncrono y veloz de Swift.
*   **Fluent ORM + Postgres Driver:** Interacción de base de datos relacional PostgreSQL con tipado seguro.
*   **PostgreSQL:** Servidor de base de datos relacional para almacenamiento de alto volumen.

### Cliente Multiplataforma (Kotlin Multiplatform)
*   **Compose Multiplatform:** UI declarativa, responsiva y compartida para Android, iOS y Web.
*   **Ktor Client (3.0.3):** Consumo de APIs multiplataforma asíncrono.
*   **Koin (4.0.0):** Service Locator e inyección de dependencias compatible con WebAssembly (Wasm).
*   **Kotlinx Serialization (1.8.0):** Deserialización JSON ultra veloz.
*   **Coil 3 (3.0.4):** Carga asíncrona de imágenes en red optimizada para KMP.
*   **Material 3:** Sistema de diseño centralizado responsivo con modo claro/oscuro automático.

### Pipeline de Datos (ETL)
*   **Python (Pandas, SQLAlchemy, Psycopg2):** Extracción, limpieza y carga (ETL) del catálogo masivo de Open Food Facts desde un archivo CSV de **12 GB** hacia PostgreSQL.

### Infraestructura y CI/CD
*   **GitHub Actions:** Pipeline automatizado paralelo para pruebas unitarias de Vapor y compilaciones nativas de KMP en servidores macOS.

---

## 📐 El Enfoque Spec-Kit: Desarrollo Autónomo Basado en Especificaciones

Este repositorio fue construido bajo el paradigma **Spec-Kit**, donde las especificaciones funcionales y de arquitectura (`.spec.md`) actúan como **documentación viva y planos estrictos de ejecución**.

### Acciones Tomadas para la Creación del Repositorio:
1.  **Auditoría e Investigación Inicial:** Se analizó el estado de las especificaciones y se levantó la base de datos PostgreSQL, poblándola con **3,744 productos limpios y certificados libres de gluten** mediante el ETL de Python.
2.  **Alineación del Backend (Specs 01, 07 y 09):**
    *   Implementación de paginación fluida (`PageRequest`) en `GET /foods`.
    *   Creación del endpoint buscador `GET /foods/search?q={query}` integrando búsquedas parciales dinámicas (`ILIKE` de Postgres) en nombres, marcas y categorías.
    *   Integración del middleware `CORSMiddleware` en Vapor para permitir el consumo seguro desde navegadores Web sin bloqueos CORS.
    *   Reescritura completa de los tests en `GlutenFreeAPITests.swift` para validar las entidades reales `Food` (limpiando las referencias obsoletas de "Todos" del template original).
3.  **Configuración Core de KMP (Spec 02):**
    *   Definición unificada en el Version Catalog (`libs.versions.toml`) de Ktor, Koin 4, Serialization y Coil 3.
    *   Inyección de dependencias del repositorio y casos de uso en el módulo común.
4.  **Capa de Datos, Dominio y Persistencia (Specs 03 y 05):**
    *   Creación de DTOs, mappers y casos de uso (`GetAllFoodsUseCase`, `GetFoodDetailUseCase`, `SearchFoodsUseCase`).
    *   Implementación de **persistencia local offline de Favoritos** usando el patrón de diseño `expect`/`actual` respaldado por `SharedPreferences` (Android), `NSUserDefaults` (iOS) y `localStorage` (Web Wasm). Esto evitó las dependencias pesadas e inestables de Room 3.0 Alpha en entornos Web.
5.  **Capa de UI y Diseño (Specs 04, 06 y 08):**
    *   Esquema de navegación multiplataforma seguro basado en estados en `App.kt` (previniendo fallos de compilación IR en iOS).
    *   Construcción de pantallas dinámicas `FoodsListScreen` (con grid responsivo `LazyVerticalGrid`) y `FavoritesScreen`.
    *   Implementación de carrusel de categorías interactivo (`LazyRow` + `FilterChip`) y barra de búsqueda interactiva.
    *   Desarrollo de `FoodDetailScreen` con soporte completo de toggle de favoritos persistido y botón de retorno.

---

## 🚀 Resoluciones de Ingeniería de Nivel Senior

Durante el ciclo de desarrollo y pruebas en simuladores, se detectaron y resolvieron de forma robusta los siguientes desafíos técnicos críticos:

### A. El Error Fatal del Ciclo de Vida de Koin (`IllegalStateException`)
*   **Problema:** Inicializar Koin en un bloque `LaunchedEffect` en la UI causaba un crash inmediato, ya que la primera composición de Compose invocaba `koinInject()` antes de que finalizara la carga del grafo de dependencias de Koin.
*   **Solución:** Movimos de forma síncrona `initKoin()` a los **puntos de entrada nativos de cada plataforma** (`MainActivity.onCreate` en Android, `MainViewController` en iOS, y `main()` en Web Wasm) *antes* de que inicie la composición de Compose, asegurando un inicio 100% libre de fallas.

### B. El Error de Loopback del Emulador de Android (`Failed to connect to 127.0.0.1`)
*   **Problema:** En el emulador de Android, `127.0.0.1` apunta a su propia interfaz virtual, bloqueando la conexión al host. Usar la IP fija `10.0.2.2` soluciona esto en emulación, pero rompe las pruebas unitarias locales en la computadora de desarrollo (AndroidHostTest).
*   **Solución:** Creamos una variable de red dinámica `getApiBaseUrl()` con detección inteligente de entornos de prueba mediante metadatos nativos (`Build.FINGERPRINT`). Si es un test unitario, apunta a `127.0.0.1`, y si es el emulador, apunta a `10.0.2.2`.
*   Adicionalmente, configuramos el servidor Swift Vapor para escuchar en **todas las interfaces de red (`0.0.0.0`)** en lugar de solo localhost, permitiendo el puenteo correcto del emulador Android.

### C. Optimización de Rendimiento de Imágenes (Coil 3 Memory Cache)
*   **Problema:** La descarga repetitiva de imágenes en listas densas producía lag al hacer scroll y consumía red.
*   **Solución:** Configuramos un `ImageLoader` personalizado inyectado en `App.kt` que asigna el **25% de la memoria RAM del sistema** para almacenamiento en caché de mapas de bits decodificados de Coil, garantizando una carga inmediata de imágenes ya visualizadas.

### D. Higiene del Repositorio vs Dataset de 12 GB
*   **Problema:** El dataset original `foods.csv` pesa **12 Gigabytes** (y comprimido 1.2 GB), violando el límite estricto de GitHub de 100 MB por archivo.
*   **Solución:** Creamos un `.gitignore` inteligente que utiliza una exclusión absoluta de raíz (`/data/`) para no comprometer el dataset al repositorio Git local/remoto, pero lo preserva intacto localmente en el disco para que los scripts locales sigan funcionando perfectamente.

---

## 📖 Instrucciones de Ejecución Local

### 1. Iniciar el Servidor Swift Vapor
Dirígete a la carpeta del backend y ejecuta el comando de escucha en todas las interfaces:
```bash
cd GlutenFreeAPI
TLS_DISABLE=true swift run GlutenFreeAPI serve --hostname 0.0.0.0 --port 8080
```
Verifica que responda en tu navegador en `http://localhost:8080/`.

### 2. Compilar y Ejecutar la Suite de KMP
Dirígete a la carpeta `FreeGluKMP` y ejecuta las siguientes tareas según tu plataforma objetivo:

*   **Verificar todas las Pruebas Unitarias de KMP:**
    ```bash
    ./gradlew :shared:allTests
    ```
*   **Ejecutar la App en Android:**
    ```bash
    ./gradlew :androidApp:installDebug
    ```
*   **Compilar la App para la Web (Wasm Webpack):**
    ```bash
    ./gradlew :webApp:wasmJsBrowserProductionWebpack
    ```
*   **Vincular el Framework nativo de iOS (para Xcode):**
    ```bash
    ./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
    ```

---

## 🤖 Prompts Utilizados en el Desarrollo Autónomo (SDD)
Durante el desarrollo se utilizaron instrucciones semánticas sumamente concisas y de alto nivel de abstracción:

*   **Prompt para Backend:** *"Aplica la especificación de API de Vapor a FoodController.swift, implementa paginación real de Fluent y la búsqueda ILIKE en nombres, marcas y categorías. Configura CORS en configure.swift y reescribe GlutenFreeAPITests para validar Food model."*
*   **Prompt para KMP Core & DI:** *"Configura libs.versions.toml con Ktor, Koin, Serialization, Coil y Paging. Implementa initKoin en MainActivity, MainViewController y Web main.kt para que se ejecute antes de la composición y erradique el error de KoinApplication not started."*
*   **Prompt para Persistencia Offline:** *"Implementa LocalFavoritesDataSource mediante expect/actual usando SharedPreferences en Android, NSUserDefaults en iOS y localStorage en Web para una persistencia ligera e inmune a fallas de Room Wasm."*
*   **Prompt para UI, Búsqueda y Caché:** *"Integra un TextField de búsqueda y chips de categorías en FoodsListScreen. Usa delay(500L) para debounce asíncrono de red. Registre setSingletonImageLoaderFactory en App.kt habilitando un MemoryCache del 25% de la RAM para optimización de imágenes de Coil3."*

---

Desarrollado con pasión, rigor de ingeniería senior y automatización autónoma de vanguardia. 🌟
