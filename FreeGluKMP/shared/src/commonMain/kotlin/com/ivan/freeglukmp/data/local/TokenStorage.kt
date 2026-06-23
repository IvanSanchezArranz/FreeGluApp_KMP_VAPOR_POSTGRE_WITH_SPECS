package com.ivan.freeglukmp.data.local

expect class TokenStorage() {
    fun saveToken(token: String)
    fun getToken(): String?
    fun clearToken()
}
