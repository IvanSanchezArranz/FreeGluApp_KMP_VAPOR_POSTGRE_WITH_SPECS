# Spec 06: Búsqueda y Filtros en Tiempo Real

## Objetivo
Permitir a los usuarios encontrar rápidamente alimentos específicos mediante integración de barras de búsqueda y filtros por categoría conectados al backend.

## Arquitectura y Componentes
1.  **Componente UI (`SearchBar`):**
    * Ubicado en la parte superior de `FoodsListScreen`.
    * Uso de operadores `StateFlow` con `debounce(500L)` en el ViewModel para retrasar la llamada a la API hasta que el usuario termine de escribir.
2.  **Filtros de Categoría:**
    * Un `LazyRow` con `FilterChip` (ej. Snacks, Desayuno, Postres) debajo del buscador.
3.  **Lógica Backend / API:**
    * Llamadas a `GET /foods/search?q=...` re-inicializando el flujo de Paging3.

## Criterios de Aceptación
- [x] Escribir texto cancela las llamadas API en curso y realiza una nueva a los 500ms.
- [x] Al borrar el texto de búsqueda, se restablece el catálogo principal.
- [x] Seleccionar una categoría recarga el listado combinando o sustituyendo la búsqueda de texto.

## Notas de Implementación Senior
*   **Buscador e Insumo:** Implementamos `OutlinedTextField` en `FoodsListScreen.kt` como buscador directo.
*   **Mecanismo de Debounce:** Usamos `delay(500L)` de Kotlin Coroutines dentro de un bloque reactivo `LaunchedEffect(searchQuery, selectedCategory)`. Esto cancela automáticamente cualquier job de llamada a la API previo que esté en curso si el usuario sigue escribiendo, disparando la consulta definitiva a los 500ms de inactividad de manera nativa e instantánea.
*   **Filtros Combinados:** Implementamos un carrusel con un `LazyRow` y componentes `FilterChip`. La búsqueda y selección de chips se combinan de forma inteligente para filtrar localmente o consultar al backend según el estado de la barra de búsqueda, satisfaciendo al 100% las expectativas de la experiencia de usuario.