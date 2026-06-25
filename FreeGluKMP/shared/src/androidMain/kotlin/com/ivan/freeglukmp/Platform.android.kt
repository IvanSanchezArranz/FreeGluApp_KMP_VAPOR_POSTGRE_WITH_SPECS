package com.ivan.freeglukmp

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun getApiBaseUrl(): String {
    // 1. If explicit LOCAL environment is selected, force local URLs
    if (CURRENT_ENVIRONMENT == AppEnvironment.LOCAL) {
        val fingerprint = Build.FINGERPRINT
        val isUnitTest = fingerprint == null || fingerprint == "unknown" || Build.DEVICE == null || Build.BRAND == "robolectric"
        return if (isUnitTest) "http://127.0.0.1:8080" else "http://10.0.2.2:8080"
    }
    
    // 2. If explicit PRODUCTION environment is selected, force cloud URL
    if (CURRENT_ENVIRONMENT == AppEnvironment.PRODUCTION) {
        return CLOUD_BACKEND_URL
    }

    // 3. Fallback to AUTO mode: smart automatic detection of the running platform
    val fingerprint = Build.FINGERPRINT
    val isUnitTest = fingerprint == null || fingerprint == "unknown" || Build.DEVICE == null || Build.BRAND == "robolectric"
    
    // Robust emulator check (goldfish/ranchu/sdk_gphone)
    val isEmulator = isUnitTest || 
                     (fingerprint != null && (fingerprint.startsWith("generic") || fingerprint.startsWith("unknown"))) || 
                     Build.MODEL.contains("google_sdk") || 
                     Build.MODEL.contains("Emulator") || 
                     Build.MODEL.contains("Android SDK built for x86") || 
                     Build.MANUFACTURER.contains("Genymotion") || 
                     Build.HARDWARE.contains("goldfish") || 
                     Build.HARDWARE.contains("ranchu") || 
                     Build.PRODUCT.contains("sdk") || 
                     Build.PRODUCT.contains("google_sdk") || 
                     Build.PRODUCT.contains("sdk_gphone") ||
                     (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) || 
                     "google_sdk" == Build.PRODUCT

    return when {
        isUnitTest -> "http://127.0.0.1:8080"
        isEmulator -> "http://10.0.2.2:8080"
        else -> CLOUD_BACKEND_URL
    }
}

actual fun getCurrentTimeMillis(): Long = System.currentTimeMillis()