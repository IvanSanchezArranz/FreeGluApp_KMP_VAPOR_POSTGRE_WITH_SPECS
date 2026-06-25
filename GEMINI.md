# 🧠 GEMINI.md — Guía y Reglas del Proyecto FreeGluApp

Este archivo contiene las directrices de arquitectura, convenciones técnicas, flujos de datos y comandos de desarrollo que **todo agente de IA o desarrollador debe seguir de forma estricta** al operar en este repositorio.

---

## 📐 1. Visión General del Ecosistema

**FreeGluApp** es un monorepo multiplataforma diseñado para la búsqueda, filtrado y gestión local de alimentos certificados sin gluten (Gluten Free). Se compone de tres pilares principales:

1.  **Backend (GlutenFreeAPI):** API REST reactiva y asíncrona construida con **Swift Vapor 4** (Swift 6), Fluent ORM y PostgreSQL.
2.  **Cliente Multiplataforma (FreeGluKMP):** Aplicación cliente unificada en **Kotlin Multiplatform (KMP)** con UI declarativa en **Compose Multiplatform** para Android, iOS y Web Wasm/JS.
3.  **Pipeline ETL (scripts/):** Scripts de extracción, limpieza e ingesta en **Python** (Pandas + SQLAlchemy) que procesan datasets masivos (Open Food Facts) hacia la base de datos PostgreSQL.

---

## 🚨 2. Reglas de Oro y Resoluciones de Arquitectura (Mandatos Estrictos)

Cualquier cambio de código en este repositorio debe respetar de forma obligatoria las siguientes resoluciones de ingeniería:

### A. Ciclo de Vida de Koin (`IllegalStateException`)
*   **Regla:** El grafo de dependencias de Koin **NUNCA** debe inicializarse en bloqueos asíncronos o de UI de Compose (como `LaunchedEffect`).
*   **Razón:** Compose realiza llamadas a `koinInject()` durante la primera composición síncrona, causando un crash fatal si el grafo no se ha cargado.
*   **Implementación:** Inicializar Koin estrictamente de forma síncrona en los puntos de entrada nativos de cada plataforma antes de Compose:
    *   **Android:** En `MainActivity.onCreate` antes de `setContent`.
    *   **iOS:** En `MainViewController.kt` antes de crear el `ComposeUIViewController`.
    *   **Web Wasm/JS:** En `main.kt` al inicio de la ejecución.

### B. Enrutamiento de Red Dinámico Adaptativo (`getApiBaseUrl()`)
*   **Regla:** La URL base de la API debe resolverse dinámicamente mediante el patrón `expect`/`actual` para soportar de manera segura emuladores, simuladores y entornos de testing.
*   **Razón:** El emulador de Android requiere conectarse a `10.0.2.2:8080`, mientras que iOS, la Web y los tests unitarios locales de JUnit (`AndroidHostTest`) requieren estrictamente `127.0.0.1:8080`.
*   **Implementación:** Utilizar la lógica de detección inteligente en `Platform.android.kt` basada en metadatos nativos (`Build.FINGERPRINT`) para resolver a `127.0.0.1` si corre como test de JVM o a `10.0.2.2` si es un emulador real.
*   **Vapor:** El servidor backend de Swift Vapor debe escuchar siempre en `0.0.0.0` para permitir el puenteo virtual de red de Android.

### C. Persistencia Local Offline sin Room para Wasm
*   **Regla:** Evitar el uso de Room u otros ORMs inestables en entornos WebAssembly (Wasm).
*   **Implementación:** Utilizar la abstracción ligera `LocalFavoritesDataSource` implementada mediante el patrón `expect`/`actual` de Kotlin para conectarse a las tecnologías nativas de almacenamiento clave-valor optimizadas de cada entorno:
    *   **Android:** Respaldado por `SharedPreferences`.
    *   **iOS:** Respaldado por `NSUserDefaults`.
    *   **Web Wasm/JS:** Respaldado por `localStorage` síncrono del navegador.

### D. Caché y Optimización de Imágenes con Coil 3
*   **Regla:** Las listas densas de productos con imágenes deben optimizarse para prevenir lag y consumo excesivo de red.
*   **Implementación:** En `App.kt`, configurar un `ImageLoader` de Coil 3 personalizado asignando el **25% de la memoria RAM del sistema** (`maxSizePercent(context, 0.25)`) para el almacenamiento en caché de mapas de bits decodificados.

### E. Higiene de Git e Ignorados (`/data/`)
*   **Regla:** El archivo `.gitignore` raíz debe bloquear estrictamente la carpeta de datos masivos de la raíz sin afectar a los paquetes lógicos de Kotlin.
*   **Implementación:** Utilizar la barra diagonal de raíz (`/data/`) para indicar que se ignore únicamente el dataset de 12 GB, evitando ignorar recursivamente paquetes lógicos como `com.ivan.freeglukmp.data/...`.

### F. Estabilidad y Preservación de la Base de Datos
*   **Regla:** Bajo NINGÚN concepto se deben realizar acciones que rompan, vacíen, borren o destruyan la base de datos local de desarrollo (`glutenfree`), sus datos de producción o las tablas existentes.
*   **Razón:** La base de datos contiene registros históricos de alimentos, favoritos de usuarios y datos de producción acumulados. Cualquier migración, actualización o cambio en el esquema debe realizarse de forma incremental, no destructiva, y preservando siempre los datos preexistentes.

---

## 🛠️ 3. Comandos de Desarrollo y Verificación

### Backend Swift Vapor

*   **Levantar base de datos local:**
    ```bash
    docker-compose -f GlutenFreeAPI/docker-compose.yml up -d
    ```
*   **Ejecutar el Servidor (Escuchando en todas las interfaces):**
    ```bash
    cd GlutenFreeAPI
    TLS_DISABLE=true swift run GlutenFreeAPI serve --hostname 0.0.0.0 --port 8080
    ```
*   **Ejecutar Tests de API:**
    ```bash
    cd GlutenFreeAPI && swift test
    ```

### Cliente Kotlin Multiplatform (KMP)

*   **Ejecutar Tests Unitarios de KMP (Todas las plataformas):**
    ```bash
    cd FreeGluKMP && ./gradlew :shared:allTests
    ```
*   **Instalar y Ejecutar en Android (Emulador/Dispositivo):**
    ```bash
    cd FreeGluKMP && ./gradlew :androidApp:installDebug
    ```
*   **Compilar para la Web (Wasm Webpack):**
    ```bash
    cd FreeGluKMP && ./gradlew :webApp:wasmJsBrowserProductionWebpack
    ```
*   **Vincular Framework para iOS Simulator (Xcode):**
    ```bash
    cd FreeGluKMP && ./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
    ```

### Pipeline ETL de Datos (Python)

*   **Importar Productos desde CSV:**
    ```bash
    cd scripts && source venv/bin/activate
    python import_csv.py /ruta/a/foods.csv
    ```

---

## 📐 4. Convenciones de Arquitectura y Código

### Backend (Swift Vapor)
*   **Fluent ORM:** Utilizar mappers síncronos y seguros para traducir modelos de base de datos a DTOs de salida.
*   **Paginación:** Todas las rutas que retornen colecciones masivas de alimentos deben implementar paginación reactiva usando la interfaz `PageRequest` de Fluent.
*   **Búsquedas:** Implementar búsquedas parciales e insensibles a mayúsculas usando `ILIKE` sobre campos clave (nombre, marcas, categoría).
*   **CORS:** Asegurar que `CORSMiddleware` esté siempre activo en `configure.swift` para permitir el consumo desde clientes Web Wasm de producción.

### Multiplataforma (Kotlin Multiplatform + Compose)
*   **Inyección de Dependencias:** Usar Koin 4.0.0 de manera modular. No inyectar dependencias directamente en vistas de Compose si estas pueden propagarse a través del grafo.
*   **UI Declarativa (Material 3):** Mantener un esquema responsivo con soporte simétrico de modo claro y oscuro dinámico.
*   **Navegación:** Emplear navegación basada en estados compartidos en `App.kt` para evitar fallos de enlazado IR estricto en el compilador de Kotlin/Native para iOS.

---

## 🤖 5. Instrucciones para Agentes de IA (Específicas de Spec-Kit/SDD)

*   **MANDATO ABSOLUTO (MUST)**: Siempre que el usuario solicite una nueva tarea, historia o funcionalidad, el agente de IA **DEBE obligatoriamente** crear primero su correspondiente carpeta de documentación dentro de `scripts/docs/` conteniendo `spec.md`, `plan.md`, `tasks.md` y `checklists/requirements.md` siguiendo el estándar descrito a continuación, antes de proceder con cualquier modificación de código.
*   **Metodología Spec-Kit:** Ante cualquier requerimiento de nuevas funcionalidades o tareas complejas, revisa siempre la carpeta `scripts/docs/` donde residen los planos técnicos unitarios (`spec.md`, `plan.md`, `tasks.md`).
*   **Estándar de Documentación de Features:** Toda nueva funcionalidad o mejora técnica debe documentarse en una carpeta dedicada dentro de `scripts/docs/` siguiendo el patrón `<ID>-<feature-slug>/` (ej. `012-navigation-state-preservation/`). Cada carpeta debe contener de forma obligatoria los siguientes archivos con estructura uniforme:
    *   `spec.md`: Visión del producto, alcance técnico detallado por capas (Dominio, Datos, Presentación) y criterios de aceptación funcionales.
    *   `plan.md`: Plan de implementación detallado paso a paso y estrategia de verificación técnica (compilación, unit tests, testing manual).
    *   `tasks.md`: Listado granular de tareas de ingeniería asignadas con identificadores de tarea (ej. T301), descripciones técnicas, dependencias, Story Points (SP) y comandos específicos de testing.
    *   `checklists/requirements.md`: Lista de verificación (Checklist) para auditar la paridad visual, contraste WCAG, touch targets, limpieza de arquitectura (Koin, UDF) y sanidad de dependencias.
*   **Flujo de Trabajo:** Prioriza el ciclo **Research -> Strategy -> Execution**, y dentro de Execution utiliza siempre la disciplina **Plan -> Act -> Validate**. No des por completado un cambio sin validar su compilación y testeo local.
*   **Higiene:** No dejes código huérfano, imports rotos o advertencias de tipos. Modifica los archivos de forma quirúrgica y exacta.

<!-- SPECKIT START -->
For additional context about technologies to be used, project structure,
shell commands, and other important information, read the current plan
<!-- SPECKIT END -->
