package com.ivan.freeglukmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun getApiBaseUrl(): String

expect fun getCurrentTimeMillis(): Long