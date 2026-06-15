# Spec 03: Capa de Datos y Dominio KMP (Red y Casos de Uso)

## Objetivo
Establecer la comunicación entre el cliente KMP y el backend Vapor, mapeando las respuestas JSON a modelos de dominio limpios y utilizando el patrón Repository y Casos de Uso.

## Arquitectura y Componentes
1.  **Capa de Datos Remotos (`data/remote`):**
    * `FoodDTO`: Modelo `@Serializable` que refleja exactamente los nombres de los campos enviados por Vapor.
    * `ApiService`: Clase inyectable con el `HttpClient` de Ktor que implementa las llamadas HTTP (`GET /foods`, `GET /foods/:id`, `GET /foods/search`).
    * `FoodsPagingSource`: Implementación de `PagingSource` de Paging3 que maneja la lógica de pedir la página `N` e incrementar hasta llegar al final de los datos de Vapor.
2.  **Capa de Dominio (`domain`):**
    * `FoodModel`: Modelo independiente de librerías externas que consumirá la UI.
    * `FoodRepository`: Interfaz que abstrae la fuente de datos.
    * `GetAllFoodsUseCase` / `GetFoodDetailUseCase`: Clases ejecutoras que aíslan la lógica de negocio.

## Criterios de Aceptación
- [x] Se consumen correctamente los datos del puerto `8080`.
- [x] El módulo Koin `DataModule` provee todas las instancias necesarias en estado Singleton.
- [x] Los errores de red se atrapan y se envuelven en estructuras tipo `Result` o excepciones controladas.

---

## Notas de Implementación y Solución de Problemas

### Problema: `JsonConvertException` al usar Paginación

Al conectar el cliente KMP con el backend Vapor que devuelve una respuesta paginada (usando `Page<Food>`), puede ocurrir un error de deserialización en Ktor:

> `JsonConvertException: Illegal input: Expected start of the array '[', but had '{' instead`

**Causa:**
El cliente KMP (en el `ApiService` o `PagingSource`) está configurado para esperar una lista JSON directa (que empieza con `[`), pero el servidor Vapor, al paginar, devuelve un objeto JSON que envuelve la lista (que empieza con `{`).

**Ejemplo de la respuesta del servidor:**
```json
{
  "items": [...],
  "metadata": { ... }
}
```

### Solución: Mapear la Respuesta Paginada

Para solucionar esto, el cliente KMP debe ser consciente de la estructura de la respuesta paginada.

1.  **Crear DTOs para la Respuesta Paginada:**
    En la capa de datos del cliente, crear modelos que representen la respuesta completa:
    ```kotlin
    // PaginatedResponseDTO.kt
    @Serializable
    data class PaginatedResponseDTO<T>(
        val items: List<T>,
        val metadata: PageMetadataDTO
    )

    @Serializable
    data class PageMetadataDTO(
        val page: Int,
        val per: Int,
        val total: Int
    )
    ```

2.  **Actualizar `ApiService`:**
    Modificar las funciones que obtienen datos paginados para que esperen el nuevo DTO:
    ```kotlin
    // ApiService.kt
    suspend fun getFoods(page: Int, limit: Int): PaginatedResponseDTO<FoodDTO> {
        // ... la llamada a Ktor ahora decodificará al nuevo DTO
    }
    ```

3.  **Actualizar `PagingSource`:**
    Ajustar la lógica de `load` para que trabaje con el nuevo DTO:
    ```kotlin
    // FoodsPagingSource.kt
    override suspend fun load(...) {
        // ...
        val response = apiService.getFoods(...)
        val domainModels = response.items.map { it.toDomain() } // Extraer la lista de 'items'
        val totalPages = (response.metadata.total + limit - 1) / limit

        LoadResult.Page(
            data = domainModels,
            prevKey = ...,
            nextKey = if (page >= totalPages) null else page + 1 // Calcular la siguiente página con los metadatos
        )
        // ...
    }
    ```
