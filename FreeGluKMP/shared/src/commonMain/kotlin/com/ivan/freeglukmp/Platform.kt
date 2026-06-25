package com.ivan.freeglukmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

// Set this to true for local development, false to connect client apps to your live cloud server!
const val USE_LOCAL_BACKEND = false
const val CLOUD_BACKEND_URL = "https://freeglu-api.onrender.com" // Update this with your live Render Web Service URL

expect fun getApiBaseUrl(): String

expect fun getCurrentTimeMillis(): Long