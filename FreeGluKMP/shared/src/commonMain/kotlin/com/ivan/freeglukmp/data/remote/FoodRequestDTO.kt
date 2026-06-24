package com.ivan.freeglukmp.data.remote

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class FoodRequestDTO(
    val code: String,
    val name: String,
    val brand: String? = null,
    val categories: String? = null,
    val ingredients: String? = null,
    val imageUrl: String? = null,
    val countries: String? = null,
    val glutenFree: Boolean
)
