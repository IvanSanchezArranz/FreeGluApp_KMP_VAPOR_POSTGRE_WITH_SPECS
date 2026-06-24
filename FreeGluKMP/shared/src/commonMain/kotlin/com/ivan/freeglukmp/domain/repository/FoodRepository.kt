package com.ivan.freeglukmp.domain.repository

import com.ivan.freeglukmp.domain.model.FoodModel

interface FoodRepository {
    suspend fun getFoods(page: Int, per: Int): Result<List<FoodModel>>
    suspend fun searchFoods(query: String, page: Int, per: Int): Result<List<FoodModel>>
    suspend fun getFoodDetail(id: String): Result<FoodModel>
    suspend fun createFood(food: FoodModel): Result<FoodModel>
    suspend fun updateFood(id: String, food: FoodModel): Result<FoodModel>
    suspend fun deleteFood(id: String): Result<Unit>
}