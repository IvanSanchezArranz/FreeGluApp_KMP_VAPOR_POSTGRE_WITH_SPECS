package com.ivan.freeglukmp

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

actual fun getApiBaseUrl(): String = "http://127.0.0.1:8080"