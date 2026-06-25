# Checklist: Database Cloud Migration

## 1. Verificación de Exportación (Local)
- [ ] La tabla local `foods` tiene registros válidos antes de exportar.
- [ ] El volcado de base de datos se genera con la opción `--data-only` (evita comandos `CREATE TABLE`, `ALTER TABLE` que causan errores en Render).
- [ ] Se comprueba que el fichero generado no pesa 0 bytes.

## 2. Verificación de Importación (Render)
- [ ] Se utiliza la URL de conexión **Externa** de Render (ya que la Interna solo es accesible dentro de sus propios contenedores).
- [ ] Se comprueba que la importación no genera alertas de violación de clave primaria.
- [ ] Se verifica mediante consulta SQL remota que el número total de filas importadas en Render coincide al 100% con la base de datos local.
