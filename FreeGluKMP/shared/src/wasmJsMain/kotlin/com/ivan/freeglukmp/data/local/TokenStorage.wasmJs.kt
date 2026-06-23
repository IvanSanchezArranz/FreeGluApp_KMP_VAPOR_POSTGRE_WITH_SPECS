package com.ivan.freeglukmp.data.local

import kotlinx.browser.localStorage

actual class TokenStorage actual constructor() {

    actual fun saveToken(token: String) {
        localStorage.setItem("jwt_token", token)
    }

    actual fun getToken(): String? {
        return localStorage.getItem("jwt_token")
    }

    actual fun clearToken() {
        localStorage.removeItem("jwt_token")
    }
}
