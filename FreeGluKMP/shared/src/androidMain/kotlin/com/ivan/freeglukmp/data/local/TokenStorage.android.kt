package com.ivan.freeglukmp.data.local

import android.content.Context
import com.ivan.freeglukmp.AndroidContextProvider

actual class TokenStorage actual constructor() {
    private var inMemoryToken: String? = null

    private val sharedPrefs by lazy {
        AndroidContextProvider.context?.getSharedPreferences("freeglu_auth", Context.MODE_PRIVATE)
    }

    actual fun saveToken(token: String) {
        if (sharedPrefs != null) {
            sharedPrefs?.edit()?.putString("jwt_token", token)?.apply()
        } else {
            inMemoryToken = token
        }
    }

    actual fun getToken(): String? {
        return if (sharedPrefs != null) {
            sharedPrefs?.getString("jwt_token", null)
        } else {
            inMemoryToken
        }
    }

    actual fun clearToken() {
        if (sharedPrefs != null) {
            sharedPrefs?.edit()?.remove("jwt_token")?.apply()
        } else {
            inMemoryToken = null
        }
    }
}
