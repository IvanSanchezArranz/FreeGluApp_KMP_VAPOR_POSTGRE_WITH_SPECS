# Spec 04: Presentación Multiplataforma (Compose Multiplatform)

## Objetivo
Diseñar las pantallas responsivas de la aplicación (Listado y Detalle) utilizando el paradigma reactivo MVVM/MVI, asegurando consistencia visual en Android, iOS y Web.

## Arquitectura y Componentes
1.  **Gestión de Estado (ViewModels):**
    * `FoodsViewModel` que expone un `StateFlow<PagingData<FoodModel>>`.
    * `FoodDetailViewModel` que gestiona estados de carga, éxito y error para un producto concreto.
2.  **Componentes Visuales (UI):**
    * `FoodsListScreen`: Una `LazyVerticalGrid` (malla de dos o más columnas adaptativas). Cada `FoodCard` usa `AsyncImage` (Coil3) para mostrar `imageUrl` y textos para `name` y `brand`.
    * `FoodDetailScreen`: Muestra la vista enfocada de un producto con sus ingredientes y categorías. Un badge visual debe indicar claramente que es apto para celíacos.
3.  **Navegación (`NavigationWrapper`):**
    * Uso de `androidx.navigation.compose.NavHost` para rutear entre pantallas pasando el UUID por los argumentos de la ruta.

## Criterios de Aceptación
- [ ] El grid se desplaza infinitamente pidiendo más páginas (Infinite Scroll).
- [ ] Al presionar un producto, la app navega suavemente al detalle del mismo.
- [ ] Las imágenes sin URL válida muestran una imagen *placeholder* por defecto.