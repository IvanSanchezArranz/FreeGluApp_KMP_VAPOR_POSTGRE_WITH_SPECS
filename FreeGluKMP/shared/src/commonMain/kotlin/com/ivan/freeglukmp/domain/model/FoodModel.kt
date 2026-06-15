package com.ivan.freeglukmp.domain.model

data class FoodModel(
    val id: String,
    val code: String,
    val name: String,
    val brand: String,
    val categories: List<String>,
    val ingredients: String,
    val imageUrl: String,
    val isGlutenFree: Boolean
)