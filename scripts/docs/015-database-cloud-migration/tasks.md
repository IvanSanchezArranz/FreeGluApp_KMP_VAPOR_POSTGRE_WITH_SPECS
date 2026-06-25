# Actionable Tasks: Database Cloud Migration

This file contains the exact dependency-ordered checklists and verification commands to implement the Database Cloud Migration.

---

## Phase 1: Database Replication

### T1501 DB-01 Exportar datos de alimentos en local (`foods_dump.sql`)
*   **Descripción**: Ejecutar `pg_dump` para realizar un volcado parcial (únicamente de registros de datos, excluyendo la creación de tablas para que no haya conflictos con Fluent) de la tabla `foods`.
*   **Precondiciones**: Base de datos local activa.
*   **Estimación**: 1 SP / 0.25 dev-days
*   **Owner**: @owner-devops
*   **Acceptance Criteria**:
    *   Se genera el archivo `/tmp/foods_dump.sql` conteniendo únicamente las instrucciones `INSERT INTO "foods"`.
*   **Test comando**: `head -n 20 /tmp/foods_dump.sql`

### T1502 DB-02 Importar datos de alimentos en la base de datos de Render
*   **Descripción**: Ejecutar `psql` para conectar con el servidor PostgreSQL externo de Render e inyectar el volcado de datos generado.
*   **Precondiciones**: T1501, base de datos de Render activa.
*   **Estimación**: 2 SP / 0.25 dev-days
*   **Owner**: @owner-devops
*   **Acceptance Criteria**:
    *   Todas las filas se insertan con éxito sin errores de sintaxis ni violaciones de integridad.
*   **Test comando**: Ejecutar query remota `SELECT COUNT(*) FROM foods;` y confirmar coincidencia con local.
