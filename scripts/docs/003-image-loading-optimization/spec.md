# Feature Specification: Image Loading Optimization

**Feature Branch**: `003-image-loading-optimization`
**Status**: Draft

## Vision & Product Overview
Images must load with high-fidelity visual transitions, and survive app lifecycles. We will configure a custom Coil 3 `ImageLoader` supporting persistent Disk Cache for native platforms while preserving standard browser caches for Web platforms.

## Technical Architecture

### 1. Platform Caching Directories (`expect` / `actual`)
- **Common (`commonMain`):** `expect fun getCacheDir(context: PlatformContext): okio.Path?`
- **Android (`androidMain`):** `context.cacheDir.resolve("image_cache").absolutePath.toPath()`
- **iOS (`iosMain`):** Resolve `NSCachesDirectory` inside NSSearchPath and return path.
- **JS / Wasm (`jsMain` & `wasmJsMain`):** Return `null`.

### 2. Coil3 ImageLoader Configuration
Update `setSingletonImageLoaderFactory` in `App.kt`:
- Configure `.crossfade(true)` for seamless visual fade-ins.
- Read `getCacheDir(context)`:
  - If not `null`, initialize `DiskCache.Builder()` with directory path and limit to `512MB` (`512 * 1024 * 1024` bytes).
- Retain the `25%` Memory Cache allocation already present.

## Acceptance Criteria
- [x] Images load with a smooth progressive desaturating or crossfading animation.
- [x] On mobile devices (Android/iOS), images are cached locally on disk (up to 512 MB).
- [x] If offline, previously loaded images continue to render instantly from the local disk cache upon restarting the app.
- [x] Web target compiles and runs flawlessly without attempting direct file system caching.