package com.ivan.freeglukmp

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun getApiBaseUrl(): String {
    val fingerprint = Build.FINGERPRINT
    val isUnitTest = fingerprint == null || fingerprint == "unknown" || Build.DEVICE == null || Build.BRAND == "robolectric"
    
    // Check if running on an emulator
    val isEmulator = isUnitTest || 
                     (fingerprint != null && (fingerprint.startsWith("generic") || fingerprint.startsWith("unknown"))) || 
                     Build.MODEL.contains("google_sdk") || 
                     Build.MODEL.contains("Emulator") || 
                     Build.MODEL.contains("Android SDK built for x86") || 
                     Build.MANUFACTURER.contains("Genymotion") || 
                     (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) || 
                     "google_sdk" == Build.PRODUCT

    return when {
        isUnitTest -> "http://127.0.0.1:8080"
        isEmulator -> "http://10.0.2.2:8080"
        else -> CLOUD_BACKEND_URL
    }
}

actual fun getCurrentTimeMillis(): Long = System.currentTimeMillis()