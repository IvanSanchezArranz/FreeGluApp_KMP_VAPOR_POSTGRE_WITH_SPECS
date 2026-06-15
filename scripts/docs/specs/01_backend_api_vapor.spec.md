# Spec 01: Backend API REST (Swift Vapor)

## Objetivo
Levantar un servidor web con Swift Vapor que se conecte a la base de datos PostgreSQL mediante Fluent ORM y sirva los datos del catálogo alimenticio a las aplicaciones cliente a través de una API RESTful JSON.

## Arquitectura y Componentes
1.  **Configuración de Vapor:**
    * Driver: `fluent-postgres-driver` en `Package.swift`.
    * Conexión: Configurada en `configure.swift` apuntando a `localhost` o contenedor Docker.
2.  **Modelo de Datos (`Food.swift`):**
    * Mapeo exacto de la tabla `foods` usando anotadores de Fluent (`@ID`, `@Field`, `@OptionalField`).
3.  **Controlador (`FoodController.swift`):**
    * Manejo de rutas agrupadas bajo `/foods`.
4.  **Endpoints Requeridos:**
    * `GET /foods`: Retorna el catálogo. Debe implementar paginación (PageRequest).
    * `GET /foods/:foodID`: Retorna los detalles de un producto específico mediante su UUID.
    * `GET /foods/search?q={query}`: Busca productos por nombre. La búsqueda debe ser **insensible a mayúsculas y minúsculas** (usando operadores `ILIKE` o trigramas en PostgreSQL).

## Criterios de Aceptación
- [x] El servidor Vapor se ejecuta en `http://127.0.0.1:8080` de manera estable.
- [x] `GET /foods` retorna un listado paginado en formato JSON con cabeceras `Content-Type: application/json`.
- [x] `GET /foods/UUID` retorna exitosamente el detalle o arroja error HTTP 404 Not Found si no existe.
- [x] Soporte para Dockerización listo (Dockerfile, docker-compose).

---

## Notas de Implementación y Solución de Problemas

### Problema: Error de compilación con `ILIKE`

Durante el desarrollo, puede surgir un error de compilación como `Cannot find operator '~~*' in scope` o `Type 'DatabaseQuery.Filter.Method' has no member 'ilike'`.

Esto ocurre cuando el compilador de Swift, por problemas de caché o configuración del entorno, no reconoce las funciones específicas del driver de PostgreSQL (`FluentPostgresDriver`), incluso si las importaciones y dependencias en `Package.swift` son correctas.

### Solución Robusta para `ILIKE`

Si limpiar el caché de compilación (`swift package clean` o `Product > Clean Build Folder` en Xcode) no resuelve el problema, la solución más robusta es evitar los operadores de conveniencia y usar una consulta SQL parametrizada directamente en el filtro de Fluent.

**Ejemplo de la solución aplicada:**

```swift
// En lugar de:
// orGroup.filter(\.$name, .ilike, "%\(term)%")

// Usar la sintaxis de SQL parametrizado:
let searchTerm = "%\(term)%"
orGroup.filter(.sql(embed: "name ILIKE \(bind: searchTerm)"))
```

Esta sintaxis logra el mismo resultado (`ILIKE`) pero de una forma más explícita que no depende de que el compilador resuelva correctamente los operadores específicos del driver, evitando así los errores de compilación.

### Problema: Advertencias de Deprecación en la Configuración de la Base de Datos

Las versiones recientes de Vapor y Fluent han actualizado la forma de configurar la conexión a la base de datos, lo que genera una cadena de advertencias de deprecación y errores de compilación (`'PostgresConfiguration' is deprecated`, `Missing argument for parameter 'tls'`).

### Solución Robusta para la Configuración de la Base de Datos

La forma moderna y correcta es usar un objeto `SQLPostgresConfiguration` y pasarle la configuración de TLS de forma explícita. Para un entorno de desarrollo local que se conecta a `localhost`, la opción más simple y segura es `tls: .disable`, ya que la conexión no viaja por una red pública.

**Ejemplo de la solución aplicada en `configure.swift`:**

```swift
// Reemplazar la configuración antigua:
// app.databases.use(.postgres(hostname: "localhost", ...), as: .psql)

// Por la nueva sintaxis:
let postgresConfig = SQLPostgresConfiguration(
    hostname: "localhost",
    username: "admin",
    password: "admin",
    database: "glutenfree",
    tls: .disable
)
app.databases.use(.postgres(configuration: postgresConfig), as: .psql)
```
