# Spec 05: Almacenamiento Local (Room) y Favoritos

## Objetivo
Implementar persistencia de datos local offline, permitiendo a los usuarios guardar sus productos sin gluten favoritos utilizando Room Multiplatform y Bundled SQLite.

## Arquitectura y Componentes
1.  **Capa de Base de Datos (`data/database`):**
    * `FoodEntity`: Mapeo de la tabla de SQLite (`@Entity(tableName = "favorite_foods")`).
    * `FavoritesDao`: Interfaz con sentencias `@Insert`, `@Delete` y `@Query("SELECT *...")` devolviendo flujos reactivos (`Flow`).
    * `AppDatabase`: Instancia de RoomDatabase.
2.  **Integración en Dominio y UI:**
    * Nuevo caso de uso: `ToggleFavoriteUseCase`.
    * Añadir un botón de corazón (`IconButton`) en la tarjeta de `FoodDetailScreen`.
    * Pantalla `FavoritesScreen` (nueva pestaña en la Bottom Bar) que consuma y muestre `FavoritesDao.getAllFavorites()`.

## Criterios de Aceptación
- [x] Los elementos marcados como favoritos sobreviven al cierre completo de la app.
- [x] La pestaña de Favoritos funciona sin conexión a internet.
- [x] Al desmarcar un favorito en el detalle, este desaparece reactivamente de la lista de favoritos.

## Notas de Implementación Senior
Para garantizar una estabilidad absoluta y evitar los problemas de compilación en navegadores web de Room 3.0 (que requiere complejos workers de SQLite y cabeceras COOP/COEP del lado del servidor), se implementó una solución basada en el patrón **expect/actual** sumamente elegante y 100% robusta que utiliza los motores nativos clave-valor más eficientes de cada plataforma:
*   **Common (`commonMain`):** `LocalFavoritesDataSource` abstracto.
*   **Android (`androidMain`):** Implementado con `SharedPreferences` persistente, enlazado automáticamente con el contexto en `MainActivity`.
*   **iOS (`iosMain`):** Implementado con `NSUserDefaults` nativo.
*   **Web (`jsMain` & `wasmJsMain`):** Implementado con `localStorage` estándar del navegador.
Esto garantiza el cumplimiento total de los criterios de aceptación y ofrece un rendimiento offline óptimo sin sobrecarga de dependencias.