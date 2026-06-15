package com.ivan.freeglukmp.data.local

import android.content.Context
import com.ivan.freeglukmp.AndroidContextProvider

actual class LocalFavoritesDataSource actual constructor() {
    private val sharedPrefs by lazy {
        AndroidContextProvider.context?.getSharedPreferences("freeglu_favorites", Context.MODE_PRIVATE)
    }

    actual fun isFavorite(id: String): Boolean {
        val favorites = getAllFavoritesSet()
        return favorites.contains(id)
    }

    actual fun toggleFavorite(id: String) {
        val favorites = getAllFavoritesSet().toMutableSet()
        if (favorites.contains(id)) {
            favorites.remove(id)
        } else {
            favorites.add(id)
        }
        sharedPrefs?.edit()?.putStringSet("favorite_ids", favorites)?.apply()
    }

    actual fun getAllFavorites(): List<String> {
        return getAllFavoritesSet().toList()
    }

    private fun getAllFavoritesSet(): Set<String> {
        return sharedPrefs?.getStringSet("favorite_ids", emptySet()) ?: emptySet()
    }
}