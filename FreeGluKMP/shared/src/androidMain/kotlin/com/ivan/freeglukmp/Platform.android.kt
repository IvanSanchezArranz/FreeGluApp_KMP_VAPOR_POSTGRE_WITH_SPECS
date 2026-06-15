package com.ivan.freeglukmp

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun getApiBaseUrl(): String {
    val fingerprint = Build.FINGERPRINT
    val isUnitTest = fingerprint == null || fingerprint == "unknown" || fingerprint.startsWith("generic") || Build.DEVICE == null || Build.BRAND == "robolectric"
    return if (isUnitTest) {
        "http://127.0.0.1:8080"
    } else {
        "http://10.0.2.2:8080"
    }
}