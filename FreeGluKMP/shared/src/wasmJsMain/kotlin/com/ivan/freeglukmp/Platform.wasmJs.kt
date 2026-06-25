package com.ivan.freeglukmp

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

actual fun getApiBaseUrl(): String {
    val hostname = kotlinx.browser.window.location.hostname
    val isLocal = hostname == "localhost" || hostname == "127.0.0.1" || hostname.isEmpty()
    return if (isLocal) {
        "http://127.0.0.1:8080"
    } else {
        CLOUD_BACKEND_URL
    }
}

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun jsDateNow(): Double = js("Date.now()")

actual fun getCurrentTimeMillis(): Long = jsDateNow().toLong()