# Spec: Implementación de la Feature `[EntityName]`

## 1. Contexto y Objetivo
Se requiere implementar el ciclo completo (End-to-End) en el cliente KMP para la entidad `[EntityName]`. Esto abarca desde el consumo de la API REST hasta la representación visual reactiva en Compose Multiplatform, utilizando Ktor, Koin y ViewModels.

---

## 2. Capa de Dominio (Reglas de Negocio)

**Archivo 1:** `shared/src/commonMain/kotlin/com/ivan/sdd/[project_name]/domain/model/[EntityName].kt`
*   **Instrucciones:** Crea una `data class` pura de Kotlin (sin anotaciones de librerías externas) que represente un `[EntityName]`.
*   **Campos:** `[lista_de_campos_de_dominio]`

**Archivo 2:** `shared/src/commonMain/kotlin/com/ivan/sdd/[project_name]/domain/repository/[EntityName]Repository.kt`
*   **Instrucciones:** Define la interfaz del repositorio. Debe contener funciones `suspend` o devolver un `Flow` para las operaciones básicas (ej. `get[EntityName]s()`).

**Archivo 3:** `shared/src/commonMain/kotlin/com/ivan/sdd/[project_name]/domain/usecase/Get[EntityName]sUseCase.kt`
*   **Instrucciones:** Crea una clase que inyecte el `[EntityName]Repository` y exponga la función `operator fun invoke()`.

---

## 3. Capa de Datos (Red y Repositorio)

**Archivo 4:** `shared/src/commonMain/kotlin/com/ivan/sdd/[project_name]/data/remote/dto/[EntityName]DTO.kt`
*   **Instrucciones:** Crea una clase `@Serializable`. Debe coincidir exactamente con el JSON de la API.
*   **Campos JSON:** `[lista_de_campos_json]`
*   **Mappers:** Incluye una función de extensión `fun [EntityName]DTO.toDomain(): [EntityName]` para convertirlo al modelo de UI.

**Archivo 5:** `shared/src/commonMain/kotlin/com/ivan/sdd/[project_name]/data/remote/[EntityName]ApiService.kt`
*   **Instrucciones:** Inyecta el `HttpClient` de Ktor. Crea funciones `suspend` que hagan peticiones HTTP `GET` al endpoint `[endpoint_url]` y devuelvan `[EntityName]DTO`.

**Archivo 6:** `shared/src/commonMain/kotlin/com/ivan/sdd/[project_name]/data/repository/[EntityName]RepositoryImpl.kt`
*   **Instrucciones:** Implementa la interfaz `[EntityName]Repository`. Inyecta el `[EntityName]ApiService`, ejecuta la llamada de red, captura errores y mapea los DTOs usando `.toDomain()`.

---

## 4. Capa de Presentación (UI y Estado)

**Archivo 7:** `shared/src/commonMain/kotlin/com/ivan/sdd/[project_name]/ui/[entity_name_lower]/[EntityName]State.kt`
*   **Instrucciones:** Crea una `sealed interface` para manejar los estados de la UI: `Loading`, `Success(val data: List<[EntityName]>)`, y `Error(val message: String)`.

**Archivo 8:** `shared/src/commonMain/kotlin/com/ivan/sdd/[project_name]/ui/[entity_name_lower]/[EntityName]ViewModel.kt`
*   **Instrucciones:** Extiende de `ViewModel`. Inyecta los Casos de Uso. Expón un `StateFlow<[EntityName]State>`. Inicializa la carga de datos en el bloque `init { }` usando `viewModelScope.launch`.

**Archivo 9:** `shared/src/commonMain/kotlin/com/ivan/sdd/[project_name]/ui/[entity_name_lower]/[EntityName]Screen.kt`
*   **Instrucciones:** Crea un componente `@Composable`. Recibe el ViewModel inyectado por Koin (`koinViewModel()`). Observa el estado con `collectAsState()` y renderiza un `CircularProgressIndicator` para carga, un texto para error, y un `LazyVerticalGrid` (o `LazyColumn`) para el caso de éxito.

---

## 5. Inyección de Dependencias (DI)

**Archivo 10:** `shared/src/commonMain/kotlin/com/ivan/sdd/[project_name]/di/DataModule.kt` (o equivalente)
*   **Instrucciones:** Actualiza el módulo de Koin para registrar `singleOf(::[EntityName]ApiService)`, `singleOf(::[EntityName]RepositoryImpl) { bind<[EntityName]Repository>() }`, los casos de uso y `viewModelOf(::[EntityName]ViewModel)`.

---

## Criterios de Aceptación
1.  La compilación es exitosa.
2.  El flujo de datos desde el DTO hasta el Screen no rompe la Clean Architecture (la UI no conoce el DTO, solo el modelo de dominio).
3.  El módulo de Koin levanta sin arrojar excepciones de dependencias no encontradas.