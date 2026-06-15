# Implementation Plan: Image Loading Optimization

**Branch**: `003-image-loading-optimization`

## Summary
Configure Coil 3 with persistent disk caching across native platforms (Android, iOS) while adding native-like Crossfade animations to all network image loads.

## Project Structure (Target Files)
```text
FreeGluApp/
└── FreeGluKMP/
    └── shared/src/
        ├── commonMain/kotlin/com/ivan/freeglukmp/utils/CachePath.kt
        ├── androidMain/kotlin/com/ivan/freeglukmp/utils/CachePath.android.kt
        ├── iosMain/kotlin/com/ivan/freeglukmp/utils/CachePath.ios.kt
        ├── jsMain/kotlin/com/ivan/freeglukmp/utils/CachePath.js.kt
        ├── wasmJsMain/kotlin/com/ivan/freeglukmp/utils/CachePath.wasmJs.kt
        └── commonMain/kotlin/com/ivan/freeglukmp/App.kt
```

## Implementation Steps
1. Create `CachePath.kt` expect function in commonMain.
2. Provide `actual` directories for Android (cacheDir), iOS (NSCachesDirectory), and null for Web (JS/Wasm).
3. Connect `getCacheDir()` in `App.kt` singleton image loader factory.
4. Add `.crossfade(true)` and `.diskCache { ... }` builder configurations.
5. Validate compiling and tests.