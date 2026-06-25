# Feature Specification: Database Cloud Migration

**Feature Branch**: `015-database-cloud-migration`
**Status**: Draft (Under Review)

## Vision & Product Overview
To ensure the live Render deployment of FreeGluApp functions identically to the local development environment, the production PostgreSQL database hosted on Render must be populated with the same validated food datasets currently available in the local PostgreSQL instance. This specification defines the mechanism and procedures for safely exporting, transforming, and importing/restoring the local `glutenfree` database into Render.

---

## Technical Scope

### 1. Replicating Schema and Constraints
- The schema is managed by Fluent ORM in the Vapor backend.
- Before restoring any data, the Render backend must be run at least once to create the core tables (`users`, `user_favorites`, `foods`), preventing any table structure mismatches.

### 2. Migration Options
- **Option A (SQL Dump/Restore)**: Use native PostgreSQL command-line tools (`pg_dump` and `psql` or `pg_restore`) to export the local table data and stream it directly over SSL to the Render PostgreSQL external database host.
- **Option B (ETL Script Direct Ingestion)**: Run the Python ETL import pipeline (`import_csv.py`) from the local machine, overriding the `DATABASE_URL` connection string to point to Render's **External Database URL**.

---

## Criterios de Aceptación
- [ ] Todos los registros de alimentos y categorías del dataset local se insertan con éxito en el servidor PostgreSQL de Render.
- [ ] No se produce ninguna violación de claves primarias, claves foráneas ni de unicidad durante la restauración.
- [ ] La app KMP conectada al servidor de Render puede consultar y buscar productos de forma inmediata con idénticos resultados que en local.
