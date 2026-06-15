package com.ivan.freeglukmp.data.local

expect class LocalFavoritesDataSource() {
    fun isFavorite(id: String): Boolean
    fun toggleFavorite(id: String)
    fun getAllFavorites(): List<String>
}