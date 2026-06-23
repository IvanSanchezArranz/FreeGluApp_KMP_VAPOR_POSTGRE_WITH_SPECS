# Feature Specification: User Authentication and Login

**Feature Branch**: `001-user-auth`

**Created**: 2026-06-23

**Status**: Ready

**Input**: User description: "Implementar autenticación de usuario (JWT) con endpoints de registro/login en Vapor y Ktor, flujo de autenticación y persistencia de tokens en KMP, y pantallas de UI correspondientes en Compose con opción de fusionar favoritos."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Registro de Usuario (Priority: P1)

Como usuario de FreeGluApp, quiero poder registrarme con mi correo y contraseña para crear una cuenta personal y sincronizar mis datos.

**Why this priority**: Es la base del sistema de identidad. Sin el registro, no es posible identificar al usuario de manera única.

**Independent Test**: Puede probarse de manera independiente enviando una petición POST a `/register` en el backend local, verificando que se devuelva un código HTTP 201 y un JSON conteniendo un JWT válido.

**Acceptance Scenarios**:

1. **Given** un correo no registrado ("test@example.com") y una contraseña segura, **When** el usuario envía el formulario de registro, **Then** el sistema crea la cuenta, hashea la contraseña de manera segura y devuelve HTTP 201 con un token JWT firmado.
2. **Given** un correo que ya está registrado en el sistema, **When** el usuario intenta registrarse de nuevo, **Then** el sistema devuelve un código HTTP 409 (Conflict) indicando que el correo ya está en uso.

---

### User Story 2 - Login de Usuario con Password (Priority: P1)

Como usuario registrado, quiero iniciar sesión con mi correo y contraseña para acceder a mis favoritos y datos sincronizados.

**Why this priority**: Permite recuperar la sesión e identificar de manera segura al usuario recurrente.

**Independent Test**: Puede probarse enviando una petición POST a `/login` con credenciales válidas y verificando que retorne un JWT.

**Acceptance Scenarios**:

1. **Given** credenciales correctas (correo y contraseña válidos), **When** el usuario inicia sesión, **Then** el sistema devuelve HTTP 200 con un JWT firmado que expira en 24 horas.
2. **Given** credenciales incorrectas (contraseña errónea), **When** el usuario inicia sesión, **Then** el sistema devuelve HTTP 401 (Unauthorized) impidiendo el acceso.

---

### User Story 3 - Integración del Flujo de Autenticación y UI en KMP (Priority: P2)

Como usuario de la aplicación móvil o web, quiero ver pantallas de Login/Registro bien estructuradas y que mis tokens se persistan de forma segura.

**Why this priority**: Proporciona el punto de interacción visual para el usuario y asegura la persistencia de la sesión offline sin requerir relogueos constantes.

**Independent Test**: Compilar y correr el cliente KMP, realizar login exitoso, cerrar la aplicación, abrirla de nuevo y verificar que el usuario permanece autenticado gracias al almacenamiento local seguro.

**Acceptance Scenarios**:

1. **Given** la aplicación se inicia por primera vez, **When** el usuario no está autenticado, **Then** se muestra la pantalla de Login con opción de acceder como invitado.
2. **Given** un login exitoso, **When** el token JWT es recibido, **Then** se almacena en el almacenamiento clave-valor nativo (`SharedPreferences`/`NSUserDefaults`/`localStorage`) de forma segura y se navega a la pantalla principal.

---

### User Story 4 - Fusión de Favoritos Anónimos al Iniciar Sesión (Priority: P2)

Como usuario que ha guardado favoritos de forma anónima, quiero que se me pregunte si deseo fusionar estos favoritos con mi cuenta al iniciar sesión.

**Why this priority**: Previene la pérdida silenciosa de datos y permite al usuario unificar su experiencia anónima previa con su cuenta en la nube.

**Independent Test**: Guardar 3 alimentos en favoritos locales sin iniciar sesión, realizar login exitoso, y comprobar en la ventana de confirmación que los 3 alimentos se han sincronizado con la base de datos del backend.

**Acceptance Scenarios**:

1. **Given** favoritos locales anónimos guardados en el dispositivo, **When** el usuario inicia sesión correctamente, **Then** el sistema presenta un diálogo explícito ofreciendo: (1) Fusionar favoritos locales con la cuenta, o (2) Mantener los de la cuenta solamente.

---

### Edge Cases

- **Expiración del Token JWT**: Qué ocurre si el token expira mientras la app está abierta. El cliente KMP debe capturar el HTTP 401 en las peticiones y redirigir limpiamente al usuario a la pantalla de Login con un mensaje amigable.
- **Entrada de Datos Inválida**: Validación de formatos de email y contraseñas débiles (menos de 8 caracteres). Debe validarse tanto en el cliente (Compose) de forma interactiva como en el servidor de forma estricta.
- **Desincronización de Favoritos en Recomposición**: Al navegar de vuelta desde el catálogo (con nuevos favoritos anónimos), el estado de `LoginScreen`/`RegisterScreen` no debe cachear la lista de favoritos mediante `remember` sin llaves, para asegurar que la fusión se realiza con los datos más actualizados de la plataforma.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema MUST permitir el registro de usuarios con email y contraseña, hasheando esta última con Bcrypt.
- **FR-002**: El sistema MUST emitir un token JWT firmado de forma criptográfica tras registro o login exitoso.
- **FR-003**: El cliente KMP MUST almacenar el token de forma segura en la plataforma nativa respectiva.
- **FR-004**: Los endpoints protegidos (ej. sincronización de favoritos) MUST requerir el header `Authorization: Bearer <token>`.
- **FR-005**: El cliente KMP MUST ofrecer la fusión de favoritos locales existentes al realizar el primer inicio de sesión.
- **FR-006**: El sistema MUST permitir añadir y eliminar favoritos de forma individual para usuarios autenticados mediante endpoints POST `/favorites/{foodID}` y DELETE `/favorites/{foodID}`.
- **FR-007**: El cliente KMP MUST mantener un cache en memoria de favoritos del servidor para permitir consultas síncronas instantáneas en UI (optimistic updates) y sincronizarse al iniciar sesión o abrir la app.
- **FR-008**: Los casos de uso de favoritos (`GetFavoriteFoodsUseCase`, `ToggleFavoriteUseCase`, `IsFavoriteUseCase`) MUST resolver de forma transparente contra el backend si el usuario está autenticado, o contra el almacenamiento local si es invitado.
- **FR-009**: El cliente KMP MUST restaurar de forma automática la sesión del usuario al arrancar la aplicación si se encuentra un token JWT válido, saltando directamente al listado de alimentos (catálogo) sin pasar por la pantalla de login.
- **FR-010**: El cliente KMP MUST proveer un método atómico de vaciado local (`clearAll()`) en `LocalFavoritesDataSource` para limpiar los favoritos locales en una sola transacción libre de condiciones de carrera asíncronas.
- **FR-011**: El proyecto de iOS MUST configurar `NSAppTransportSecurity` en `Info.plist` para autorizar conexiones HTTP no seguras a `127.0.0.1` (servidor local de desarrollo de Vapor).
- **FR-012**: Las implementaciones `actual` de iOS de persistencia local MUST invocar `.synchronize()` sobre `NSUserDefaults` para garantizar la persistencia física e inmediata de tokens y favoritos.

### Key Entities

- **User**: Representa la cuenta del usuario autenticado. Atributos: `id` (UUID), `email` (String, unique), `passwordHash` (String), `createdAt` (Timestamp).
- **UserFavorite**: Relación de favoritos de un usuario. Atributos: `id` (UUID), `userId` (UUID, FK to User), `foodId` (UUID, FK to Food), `createdAt` (Timestamp).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Las peticiones de registro y login deben procesarse en menos de 500ms en condiciones normales.
- **SC-002**: El hasheado de contraseñas mediante Bcrypt debe utilizar una fuerza de trabajo estándar (cost/work factor de 12 en Vapor).
- **SC-003**: 100% de cobertura en tests unitarios para los controladores de autenticación y los repositorios de KMP.

## Assumptions

- Se asume que el backend Vapor y la base de datos PostgreSQL local están activos y escuchando en los puertos estándar correspondientes.
- La firma de tokens JWT utilizará una clave secreta cargada desde variables de entorno (`JWT_SECRET`) con un valor seguro por defecto para desarrollo local.
