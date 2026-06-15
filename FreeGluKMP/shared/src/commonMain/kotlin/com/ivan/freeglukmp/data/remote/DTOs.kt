package com.ivan.freeglukmp.data.remote

import kotlinx.serialization.Serializable

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
    val code: String,
    val name: String,
    val brand: String? = null,
    val categories: String? = null,
    val ingredients: String? = null,
    val imageUrl: String? = null,
    val countries: String? = null,
    val glutenFree: Boolean
)