package com.ivan.freeglukmp.data.remote

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class PaginatedResponseDTO<T>(
    val items: List<T>,
    val metadata: PageMetadataDTO
)

@Serializable
data class PageMetadataDTO(
    val page: Int,
    val per: Int,
    val total: Int
)

@Serializable
data class FoodDTO(
    val id: String? = null,
    val code: String = "",
    val name: String = "",
    val brand: String? = null,
    val categories: String? = null,
    val ingredients: String? = null,
    val imageUrl: String? = null,
    val countries: String? = null,
    val glutenFree: Boolean = false
)

@Serializable
data class AuthRequestDTO(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponseDTO(
    val token: String,
    val user: UserResponseDTO
)

@Serializable
data class UserResponseDTO(
    val id: String,
    val email: String
)

@Serializable
data class SyncFavoritesDTO(
    val foodIds: List<String>
)

@Serializable
data class SyncResponseDTO(
    val success: Boolean,
    val syncedCount: Int
)

@Serializable
data class ErrorResponseDTO(
    val error: Boolean,
    val reason: String? = null
)