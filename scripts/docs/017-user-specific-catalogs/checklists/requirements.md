# Checklist: User-Specific Food Catalogs (Copy-on-Write)

## 1. Verificación de Seguridad y Permisos
- [ ] Las consultas de anulación de alimentos se filtran estrictamente por el `user_id` del token JWT verificado.
- [ ] Un usuario no puede ver ni modificar los alimentos creados o modificados por otro usuario (las anulaciones de otros usuarios se aíslan completamente).
- [ ] No se permite acceso anónimo o sin token para escribir o borrar en la base de datos de catálogos personalizados (las llamadas devuelven `401 Unauthorized`).

## 2. Paridad e Integridad de Datos
- [ ] La base de datos local `glutenfree` mantiene intactos sus 3,743 productos globales iniciales para nuevos registros.
- [ ] Se verifica que al borrar un producto base, la respuesta de `/foods` del usuario ejecutor no lo incluye, mientras que la respuesta para otros usuarios sigue mostrándolo.
- [ ] El paginado sigue funcionando de forma fluida y correcta después de aplicar el join de anulaciones de usuario en base de datos.
