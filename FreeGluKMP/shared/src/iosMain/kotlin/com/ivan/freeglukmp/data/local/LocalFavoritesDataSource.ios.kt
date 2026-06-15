package com.ivan.freeglukmp.data.local

import platform.Foundation.NSUserDefaults

actual class LocalFavoritesDataSource actual constructor() {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun isFavorite(id: String): Boolean {
        val list = getAllFavorites()
        return list.contains(id)
    }

    actual fun toggleFavorite(id: String) {
        val list = getAllFavorites().toMutableList()
        if (list.contains(id)) {
            list.remove(id)
        } else {
            list.add(id)
        }
        defaults.setObject(list, "favorite_ids")
    }

    actual fun getAllFavorites(): List<String> {
        val stored = defaults.arrayForKey("favorite_ids") ?: emptyList<Any?>()
        return stored.filterIsInstance<String>()
    }
}