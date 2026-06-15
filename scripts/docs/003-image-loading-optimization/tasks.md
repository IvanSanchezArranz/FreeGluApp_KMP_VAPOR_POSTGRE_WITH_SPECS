# Tasks: Image Loading Optimization

- [x] T001: Create platform abstraction `expect fun getCacheDir` using `okio.Path` and `PlatformContext` inside `shared/src/commonMain/kotlin/com/ivan/freeglukmp/utils/CachePath.kt`.
- [x] T002: Implement `actual fun getCacheDir` inside Android, iOS, JS, and WasmJs targets.
- [x] T003: Update KMP `App.kt` singleton image loader factory to integrate `getCacheDir()` and apply `.crossfade(true)` and `.diskCache` settings.
- [x] T004: Validate the changes using Gradle tests to verify compilations are successful.