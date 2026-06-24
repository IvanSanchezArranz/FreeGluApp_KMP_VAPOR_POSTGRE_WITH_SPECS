package com.ivan.freeglukmp.domain.repository

import com.ivan.freeglukmp.data.remote.UserResponseDTO
import com.ivan.freeglukmp.domain.model.FoodModel
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val favoriteIds: StateFlow<Set<String>>
    val isLoggedInState: StateFlow<Boolean>
    
    suspend fun register(email: String, password: String): Result<UserResponseDTO>
    suspend fun login(email: String, password: String): Result<UserResponseDTO>
    fun getSavedToken(): String?
    fun logout()
    fun isLoggedIn(): Boolean
    suspend fun syncFavorites(foodIds: List<String>): Result<Int>
    suspend fun getRemoteFavorites(): Result<List<FoodModel>>
    
    suspend fun addRemoteFavorite(foodId: String): Result<Unit>
    suspend fun removeRemoteFavorite(foodId: String): Result<Unit>
    suspend fun fetchAndCacheRemoteFavorites(): Result<Unit>
}
