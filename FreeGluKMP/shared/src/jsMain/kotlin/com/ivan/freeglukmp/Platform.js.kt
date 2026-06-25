package com.ivan.freeglukmp

import web.navigator.navigator

class JsPlatform: Platform {
    private val userAgent = navigator.userAgent
    private val browserList = listOf("Chrome", "Firefox", "Safari", "Edge")

    override val name: String = userAgent.findAnyOf(browserList, ignoreCase = true)
            ?.let { (startIndex) -> userAgent.substring(startIndex).substringBefore(" ") }
            ?: "Unknown"
}

actual fun getPlatform(): Platform = JsPlatform()

actual fun getApiBaseUrl(): String {
    val hostname = kotlinx.browser.window.location.hostname
    val isLocal = hostname == "localhost" || hostname == "127.0.0.1" || hostname.isEmpty()
    return if (isLocal) {
        "http://127.0.0.1:8080"
    } else {
        CLOUD_BACKEND_URL
    }
}

actual fun getCurrentTimeMillis(): Long = kotlin.js.Date.now().toLong()