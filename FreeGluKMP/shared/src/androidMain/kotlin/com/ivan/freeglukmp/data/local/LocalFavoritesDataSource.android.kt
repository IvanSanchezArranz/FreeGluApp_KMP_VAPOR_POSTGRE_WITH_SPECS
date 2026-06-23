package com.ivan.freeglukmp.data.local

import android.content.Context
import com.ivan.freeglukmp.AndroidContextProvider

actual class LocalFavoritesDataSource actual constructor() {
    private val inMemoryFavorites = mutableSetOf<String>()

    private val sharedPrefs by lazy {
        AndroidContextProvider.context?.getSharedPreferences("favorite_ids", Context.MODE_PRIVATE)
    }

    actual fun isFavorite(id: String): Boolean {
        return if (sharedPrefs != null) {
            getAllFavoritesSet().contains(id)
        } else {
            inMemoryFavorites.contains(id)
        }
    }

    actual fun toggleFavorite(id: String) {
        if (sharedPrefs != null) {
            val current = getAllFavoritesSet().toMutableSet()
            if (current.contains(id)) {
                current.remove(id)
            } else {
                current.add(id)
            }
            sharedPrefs?.edit()?.putStringSet("favorite_ids", current)?.apply()
        } else {
            if (inMemoryFavorites.contains(id)) {
                inMemoryFavorites.remove(id)
            } else {
                inMemoryFavorites.add(id)
            }
        }
    }

    actual fun getAllFavorites(): List<String> {
        return if (sharedPrefs != null) {
            getAllFavoritesSet().toList()
        } else {
            inMemoryFavorites.toList()
        }
    }

    actual fun clearAll() {
        if (sharedPrefs != null) {
            sharedPrefs?.edit()?.remove("favorite_ids")?.commit()
        } else {
            inMemoryFavorites.clear()
        }
    }

    private fun getAllFavoritesSet(): Set<String> {
        return sharedPrefs?.getStringSet("favorite_ids", emptySet()) ?: emptySet()
    }
}
