package com.ivan.freeglukmp.domain.repository

import com.ivan.freeglukmp.domain.model.FoodModel

interface FoodRepository {
    suspend fun getFoods(page: Int, per: Int): Result<List<FoodModel>>
    suspend fun searchFoods(query: String, page: Int, per: Int): Result<List<FoodModel>>
    suspend fun getFoodDetail(id: String): Result<FoodModel>
}