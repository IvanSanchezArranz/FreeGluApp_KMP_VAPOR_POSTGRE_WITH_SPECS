package com.ivan.freeglukmp.data.remote

import com.ivan.freeglukmp.domain.model.FoodModel

fun FoodDTO.toDomain(): FoodModel {
    return FoodModel(
        id = this.id?.toString() ?: "",
        code = this.code,
        name = this.name,
        brand = this.brand ?: "Unknown Brand",
        categories = this.categories?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList(),
        ingredients = this.ingredients ?: "Ingredients not available",
        imageUrl = this.imageUrl ?: "",
        isGlutenFree = this.glutenFree
    )
}