package com.ivan.freeglukmp.data.local

import platform.Foundation.NSUserDefaults

actual class TokenStorage actual constructor() {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun saveToken(token: String) {
        defaults.setObject(token, "jwt_token")
        defaults.synchronize()
    }

    actual fun getToken(): String? {
        return defaults.stringForKey("jwt_token")
    }

    actual fun clearToken() {
        defaults.removeObjectForKey("jwt_token")
        defaults.synchronize()
    }
}
