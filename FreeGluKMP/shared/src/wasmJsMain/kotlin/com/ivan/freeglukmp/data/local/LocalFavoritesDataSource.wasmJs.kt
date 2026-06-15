package com.ivan.freeglukmp.data.local

import kotlinx.browser.localStorage

actual class LocalFavoritesDataSource actual constructor() {

    actual fun isFavorite(id: String): Boolean {
        return getAllFavorites().contains(id)
    }

    actual fun toggleFavorite(id: String) {
        val list = getAllFavorites().toMutableList()
        if (list.contains(id)) {
            list.remove(id)
        } else {
            list.add(id)
        }
        localStorage.setItem("favorite_ids", list.joinToString(","))
    }

    actual fun getAllFavorites(): List<String> {
        val stored = localStorage.getItem("favorite_ids") ?: ""
        if (stored.isEmpty()) return emptyList()
        return stored.split(",")
    }
}