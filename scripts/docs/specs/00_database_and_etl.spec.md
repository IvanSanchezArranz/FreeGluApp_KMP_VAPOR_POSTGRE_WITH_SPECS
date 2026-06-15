# Spec 00: Base de Datos y Pipeline ETL

## Objetivo
Configurar la base de datos relacional PostgreSQL y establecer un pipeline ETL (Extract, Transform, Load) mediante Python para poblar el catálogo de alimentos "Gluten Free" a partir del dataset crudo de OpenFoodFacts.

## Arquitectura y Componentes
1.  **PostgreSQL (Base de datos):**
    * Base de datos: `glutenfree` administrada por el usuario `admin`.
    * Tabla `foods`: Contiene `id` (UUID), `code`, `name`, `brand`, `categories`, `ingredients`, `image_url`, `countries`, `gluten_free`, `created_at`.
    * Índices: Índice B-Tree en `name` e índice FTS/Trigrama (`pg_trgm`) para permitir búsquedas eficientes.
2.  **Python ETL (`scripts/import_csv.py`):**
    * Librerías: `pandas`, `sqlalchemy`, `psycopg2-binary`.
    * Fuente: Archivo TSV de OpenFoodFacts (`data/foods.csv`).
    * Transformación: Lee en *chunks* de 5000 líneas (`chunksize`), filtra las columnas útiles, mapea y evalúa si la columna `labels_tags` contiene el string "gluten-free". Filtra los resultados nulos o sin nombre.
    * Carga: Inserta los *chunks* validados directamente a PostgreSQL usando `to_sql`.

## Criterios de Aceptación
- [x] El servicio de PostgreSQL se ejecuta en el puerto `5432`.
- [x] La tabla `foods` y sus índices se crean correctamente sin errores de sintaxis.
- [x] El script ETL procesa el archivo TSV sin agotar la memoria RAM del sistema.
- [x] Se filtran exclusivamente productos que sean verdaderamente "Gluten Free".