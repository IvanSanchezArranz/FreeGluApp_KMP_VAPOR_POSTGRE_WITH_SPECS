# Spec 08: Sistema de Diseño y UI/UX (Design System)

## Objetivo
Establecer un sistema de diseño centralizado en Compose Multiplatform utilizando Material 3, garantizando que los colores, tipografías y componentes (botones, tarjetas) sean consistentes en iOS, Android y Web, y soporten Modo Claro/Oscuro.

## Arquitectura y Componentes
1.  **Tematización (`shared/src/commonMain/kotlin/.../theme/`):**
    * `Color.kt`: Definir la paleta "Gluten Free" (ej. tonos verdes/terrosos).
    * `Typography.kt`: Tipografías personalizadas importadas como recursos en Compose.
    * `Theme.kt`: Función `GlutenFreeTheme(darkTheme: Boolean, content: @Composable () -> Unit)`.
2.  **Componentes Reutilizables (Atoms & Molecules):**
    * Crear una carpeta `/components`.
    * `GlutenFreeButton`: Botón principal estandarizado.
    * `FoodCard`: Componente reutilizable para listas y grids.
    * `GlutenFreeBadge`: Un chip visual o icono estandarizado para marcar productos seguros.
3.  **Recursos Compartidos (`composeResources`):**
    * Centralizar iconos e imágenes estáticas (logo, placeholders) en la carpeta `shared/src/commonMain/composeResources/`.

## Criterios de Aceptación
- [ ] La app detecta automáticamente si el dispositivo (o navegador) está en modo oscuro y adapta los colores.
- [ ] No hay colores ni tamaños de fuente "quemados" (hardcoded) en las pantallas; todo usa `MaterialTheme.colorScheme` y `MaterialTheme.typography`.