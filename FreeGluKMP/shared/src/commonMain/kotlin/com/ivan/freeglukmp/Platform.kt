package com.ivan.freeglukmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

enum class AppEnvironment {
    LOCAL,     // Runs locally against your Vapor backend (127.0.0.1 / 10.0.2.2)
    PRODUCTION // Runs globally against Render (https://freeglu-api.onrender.com)
}

// 🌐 CURRENT_ENVIRONMENT: Switch this single variable to target different backends!
val CURRENT_ENVIRONMENT = AppEnvironment.LOCAL

const val CLOUD_BACKEND_URL = "https://freeglu-api.onrender.com" // Your live Render Web Service URL

expect fun getApiBaseUrl(): String

expect fun getCurrentTimeMillis(): Long