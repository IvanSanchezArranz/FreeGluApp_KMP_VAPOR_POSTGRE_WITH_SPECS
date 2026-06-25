# Implementation Plan: Database Cloud Migration

This plan outlines the step-by-step procedures to migrate your local food database records to the production database hosted on Render.

## 🛠️ Step-by-Step Implementation

### Step 1: Export Local Database Data (Dump)
Using PostgreSQL's `pg_dump` command, we will generate a clean SQL backup containing only the row inserts for the `foods` table (excluding schema creation commands to prevent conflicts with Fluent ORM's active migrations).

```bash
# Export command
pg_dump -U admin -d glutenfree --data-only -t foods -f /tmp/foods_dump.sql
```

### Step 2: Restore Data to Render (Restore)
Using PostgreSQL's `psql` command, we will stream the generated `/tmp/foods_dump.sql` data directly to the Render external database over an encrypted SSL connection.

```bash
# Import command (using the External Connection String from Render)
psql "postgres://admin:<password>@<render-db-hostname>/glutenfree" -f /tmp/foods_dump.sql
```

### Step 3: Run Python ETL directly against Render (Alternative Option)
If you prefer to run the ETL script directly to populate the Render database with a fresh or larger dataset:
1. Activate your local virtual environment: `source scripts/venv/bin/activate`.
2. Run the import script, passing Render's External Connection URL as an environment variable or directly if supported:
```bash
python scripts/import_csv.py /ruta/a/foods.csv --db-url "postgres://admin:<password>@<render-db-hostname>/glutenfree"
```

---

## 🧪 Verification Strategy

### 1. Database Row Count Check
- Query both the local and Render database to verify the matching number of entries inside the `foods` table:
```sql
SELECT COUNT(*) FROM foods;
```

### 2. Live Cloud App Verification
- Open the deployed WebApp or mobile client pointing to the Render URL, perform searches, and ensure results match local performance.
