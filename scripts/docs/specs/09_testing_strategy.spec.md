# Spec 09: Estrategia de Pruebas (QA y Testing)

## Objetivo
Asegurar la calidad del software a lo largo del tiempo mediante pruebas automatizadas en el Backend (Vapor) y en el Cliente (KMP).

## Arquitectura y Componentes
1.  **Backend Tests (Swift XCTVapor):**
    * *Nota: Ya hay un archivo `backendTests.swift` en el proyecto base.*
    * Añadir tests de integración para verificar que `GET /foods` devuelve un status 200 OK.
    * Verificar que la ruta de búsqueda `GET /foods/search?q=pizza` devuelve resultados consistentes.
2.  **KMP Domain & Data Tests (`shared/src/commonTest/`):**
    * Usar `kotlin.test` para validar la lógica pura.
    * Testear el mapeo de `FoodDTO` a `FoodModel` (asegurar que campos nulos se manejan bien).
    * *Mocking:* Usar el `MockEngine` de Ktor para simular la respuesta del servidor (JSON falso) y verificar que el `FoodRepository` la parsea sin depender de la red real.
3.  **KMP ViewModel Tests:**
    * Verificar que al llamar a `loadFoods()`, el estado inicial es `Loading` y luego pasa a `Success`.

## Criterios de Aceptación
- [ ] Ejecutar `swift test` en el backend pasa con éxito.
- [ ] Ejecutar `./gradlew :shared:allTests` pasa con éxito en la lógica compartida de Kotlin.