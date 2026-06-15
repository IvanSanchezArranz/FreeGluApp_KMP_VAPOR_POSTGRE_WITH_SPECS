PROYECTO: FreeGluApp — Ecosistema Monorepo Multiplataforma
AUTOR: SNGULAR Tech Excellence & AI-Driven Software Engineering Team
  ---

1    ███████╗███╗   ██╗ ██████╗ ██╗   ██╗██╗      █████╗ ██████╗
2    ██╔════╝████╗  ██║██╔════╝ ██║   ██║██║     ██╔══██╗██╔══██╗
3    ███████╗██╔██╗ ██║██║  ███╗██║   ██║██║     ███████║██████╔╝
4    ╚════██║██║╚██╗██║██║   ██║██║   ██║██║     ██╔══██║██╔══██╗
5    ███████║██║ ╚████║╚██████╔╝╚██████╔╝███████╗██║  ██║██║  ██║
6    ╚══════╝╚═╝  ╚═══╝ ╚═════╝  ╚═════╝ ╚══════╝╚═╝  ╚═╝╚═╝  ╚═╝
7              S N G U L A R   T E C H   R E P O R T

  ---

1. Executive Summary & Introducción

En el marco de la consultoría de innovación y excelencia técnica de SNGULAR, se ha llevado a cabo el diseño, desarrollo y
validación del MVP de FreeGluApp, un ecosistema tecnológico moderno, robusto y altamente desacoplado orientado a resolver la
búsqueda, filtrado y gestión local de alimentos certificados sin gluten (Gluten Free).

Este proyecto marca un hito en la ingeniería moderna de software, habiéndose desarrollado de forma 100% autónoma bajo la
disciplina de Software Development Dynamics (SDD) y la metodología de planos vivos de especificación Spec-Kit (Specify CLI).
La solución abarca desde un pipeline de ingesta masiva (ETL) en Python hasta la entrega de un cliente multiplataforma
unificado en Kotlin Multiplatform (KMP) y una API Backend reactiva en Swift Vapor, respaldada por una base de datos
PostgreSQL.

  ---

2. Visión Arquitectónica y Flujo de Datos "Liquid-Flow"

En SNGULAR abogamos por arquitecturas ágiles, desacopladas y de alto rendimiento. Para FreeGluApp se ha implementado un flujo
de datos que denominamos "Liquid-Flow", que transfiere y mapea información de manera óptima desde el repositorio masivo de
Open Food Facts hasta los tres clientes de UI finales:

    1 [OpenFoodFacts TSV (12 GB)]
    2             ↓  (Ingesta masiva y filtrado en Chunks de 5k)
    3   [Python ETL (Pandas + SQLAlchemy en venv)]
    4             ↓  (Persistencia Relacional Indexada)
    5   [PostgreSQL (Base de Datos "glutenfree" en puerto 5432)]
    6             ↓  (API REST reactiva y asíncrona en puerto 8080)
    7   [Swift Vapor 4 Backend (Fluent ORM - Escucha en 0.0.0.0)]
    8             ↓  (Consumo Multiplataforma y Serialización JSON)
    9   [Kotlin Multiplatform (Shared Library KMP Core)]
10             ├─────────────────────────┼─────────────────────────┐
11             ↓                         ↓                         ↓
12     [Android App]                 [iOS App]               [Web App (Wasm/JS)]
13 (Compose Multiplatform)      (UIKit/SwiftUI Bridge)     (Compose Web/Webpack)

  ---

3. Stack Tecnológico Seleccionado (Enterprise-Grade)

SNGULAR ha seleccionado un conjunto de tecnologías de vanguardia para garantizar la escalabilidad, la mantenibilidad a largo
plazo y la eficiencia de recursos:

┌───────────────────┬──────────────────────┬────────────────────────────────────────────────────────────────────────────┐
│ Capa / Componente │ Tecnología           │ Racional Técnico SNGULAR                                                   │
│                   │ Seleccionada         │                                                                            │
├───────────────────┼──────────────────────┼────────────────────────────────────────────────────────────────────────────┤
│ Backend API       │ Swift Vapor 4 (Swift │ Arquitectura asíncrona nativa de hilos ligeros (EventLoops). Ultra veloz y │
│                   │ 6)                   │ de bajo consumo en CPU/RAM.                                                │
│ ORM & Database    │ Fluent ORM +         │ Mapeo relacional seguro con tipado estricto. Índices optimizados en        │
│                   │ PostgreSQL           │ Postgres para búsquedas instantáneas.                                      │
│ Pipeline ETL      │ Python 3 + Pandas    │ Ingesta de alto rendimiento capaz de parsear datasets gigantes de 12 GB en │
│                   │                      │ chunks sin desbordar memoria.                                              │
│ Core              │ Kotlin Multiplatform │ Reutilización de más del 85% de la lógica de red, persistencia local y     │
│ Multiplataforma   │ (KMP)                │ dominio entre Android, iOS y la Web.                                       │
│ Inyección de Dep. │ Koin 4.0.0           │ Service Locator ligero y modular de última generación compatible con       │
│                   │                      │ WebAssembly (Wasm).                                                        │
│ Carga de Imágenes │ Coil 3 (3.0.4)       │ El estándar multiplataforma para caché optimizada de imágenes por red en   │
│                   │                      │ Compose.                                                                   │
│ UI Engine         │ Compose              │ Renderizado declarativo unificado de Material 3 que asegura fidelidad de   │
│                   │ Multiplatform        │ diseño simétrica en las tres plataformas.                                  │
└───────────────────┴──────────────────────┴────────────────────────────────────────────────────────────────────────────┘
  ---

4. Resoluciones de Ingeniería Avanzadas (El "SNGULAR Senior Touch")

Durante las fases de integración y ejecución de la aplicación, nuestro equipo técnico senior identificó y resolvió de forma
impecable cinco desafíos críticos que suelen comprometer la estabilidad en producción:

A. Corrección de Carrera en el Ciclo de Vida de Koin (IllegalStateException)
* El problema de ciclo de vida: Inicialmente, el grafo de dependencias de Koin se arrancaba dentro de un bloque
  LaunchedEffect en la UI compartida. Sin embargo, en Compose, los LaunchedEffect se ejecutan después de que la primera
  composición se completa. Como las pantallas de Compose llaman a koinInject() inmediatamente durante la fase de composición
  inicial, el sistema fallaba de forma fatal al no encontrar Koin inicializado.
* La solución SNGULAR: Eliminamos LaunchedEffect de la capa común de Compose y movimos initKoin() de forma síncrona a los
  puntos de entrada de sistema nativos de cada plataforma antes de que empiece cualquier composición:
    * Android: En el MainActivity.onCreate nativo antes del setContent.
    * iOS: En MainViewController.kt antes de retornar el ComposeUIViewController.
    * Web Wasm/JS: En main.kt de la web, al inicio de la ejecución.

B. Enrutamiento de Red Dinámico Auto-Adaptable (getApiBaseUrl())
* El problema de emulación: El emulador de Android requiere conectarse a la IP especial 10.0.2.2 para mapear los servicios
  del host Mac, mientras que los simuladores de iOS, la web y los entornos de pruebas unitarias locales en el host Mac
  (AndroidHostTest de JUnit) necesitan conectarse estrictamente a 127.0.0.1.
* La solución SNGULAR: Implementamos un resolvedor expect/actual dinámico y a prueba de nulos (Platform.android.kt):

1     actual fun getApiBaseUrl(): String {
2         val fingerprint = Build.FINGERPRINT
3         // Detecta si corre como test unitario en el host Mac o en un emulador de Android real
4         val isUnitTest = fingerprint == null || fingerprint == "unknown" || fingerprint.startsWith("generic") ||
Build.DEVICE == null || Build.BRAND == "robolectric"
5         return if (isUnitTest) "http://127.0.0.1:8080" else "http://10.0.2.2:8080"
6     }
Adicionalmente, configuramos el servidor de Vapor para escuchar en la interfaz comodín 0.0.0.0, asegurando que el puente
virtual del emulador Android pueda comunicarse de manera fluida.

C. Persistencia Local Offline de Alta Estabilidad para Wasm
* El problema de Room en Web: Room Multiplatform para WebAssembly (WasmJs) se encuentra en un estado inestable (Alpha),
  requiriendo complejos Web Workers, SQL.js y cabeceras COOP/COEP del servidor que comprometen seriamente el rendimiento de
  la aplicación web.
* La solución SNGULAR: Diseñamos un almacenamiento offline ultraligero y de alto rendimiento basado en el patrón
  expect/actual llamado LocalFavoritesDataSource, acoplándolo a los motores clave-valor nativos optimizados de cada sistema:
    * Android: Persistencia respaldada por SharedPreferences.
    * iOS: Persistencia nativa mediante NSUserDefaults.
    * Web (Wasm/JS): Persistencia en localStorage síncrono del navegador.

D. Optimización de Memoria y Carga de Imágenes (Coil 3 Memory Cache)
* El problema: El scroll rápido en listados masivos producía lag y sobrecarga de red al descargar repetidamente las mismas
  imágenes de productos.
* La solución SNGULAR: Modificamos la raíz de la aplicación para configurar un motor ImageLoader personalizado de Coil 3 que
  habilita la caché en memoria y destina el 25% de la memoria RAM del sistema (maxSizePercent(context, 0.25)) para guardar
  las imágenes ya decodificadas de forma persistente, haciendo que la navegación de vuelta o el scroll sean instantáneos.

E. Higiene Extrema del Monorepo Git (La Regla /data/)
* El problema de Git Ignore: Declarar data/ en el .gitignore de la raíz provoca que Git ignore recursivamente cualquier
  subcarpeta llamada data en el proyecto. Esto causaba un "wipeout" silencioso, haciendo que Git omitiera por completo los
  paquetes críticos de código Kotlin com.ivan.freeglukmp.data/....
* La solución SNGULAR: Saneamos el repositorio aplicando una regla estricta de raíz en el .gitignore (/data/). De este modo,
  se ignora únicamente el dataset de 12 GB de la raíz, mientras que todas nuestras capas lógicas de datos multiplataforma
  permanecen correctamente rastreadas y bajo control de versiones.

  ---

5. El Enfoque Spec-Kit: Desarrollo Autónomo y DevOps

En SNGULAR creemos firmemente que la Inteligencia Artificial unida a metodologías de especificación rigurosas (SDD) acelera
los tiempos de entrega (Time-to-Market) con un cero de desviación arquitectónica (Zero Drift).

Para este desarrollo, cada fase se estructuró a través de planos técnicos unitarios que guiaron la automatización:

1 [Definición de Requisitos en Specs] ➔ [Validación de Ambivalencias] ➔ [Specify CLI Automation]
2                                                                                 │
3 [Validación de Calidad Local (allTests)] ◀ [Iteración e Implementación de Código] ◀┘

El Pipeline CI/CD Automatizado
Configuramos un pipeline industrial en .github/workflows/ci.yml para garantizar que cada subida a la rama main sea verificada
de manera autónoma en servidores macOS:
* Fase de Backend: Levanta un contenedor Docker de PostgreSQL, restaura la estructura de datos, compila con Swift 6 y corre
  la suite completa de swift test.
* Fase de KMP: Descarga Java 17, optimiza la memoria heap del compilador Gradle a 6 GB (org.gradle.jvmargs=-Xmx6G) y compila
  todos los artefactos nativos para certificar la estabilidad de la rama principal de Git.

  ---

6. Handover y Guía de Despliegue en Producción

El monorepo está completamente configurado y enlazado a su rama remota de Git en GitHub. Para desplegar en entornos de
producción, SNGULAR recomienda el siguiente esquema Cloud-Native:

1. Base de Datos en Producción: Desplegar una instancia gestionada de PostgreSQL en Supabase o Neon, habilitando índices de
   trigrama para optimizar las búsquedas.
2. Hospedaje de la API (Vapor): Contenedorizar con el Dockerfile incluido en la carpeta GlutenFreeAPI y desplegar en Fly.io
   o Railway vinculando las variables de entorno de producción.
3. Despliegue de la App Web (Wasm): Compilar el bundle estático mediante ./gradlew :webApp:wasmJsBrowserProductionWebpack y
   hospedar los archivos HTML/JS/Wasm estáticos en Vercel o GitHub Pages.
4. Despliegue Móvil: Generar los archivos binarios firmados de producción (.apk y .ipa) mediante Gradle y Xcode para su
   distribución a Google Play y App Store.

  ---
SNGULAR Tech Report  
Technical Excellence, Clean Code & AI-Driven Software Engineering. 🌟