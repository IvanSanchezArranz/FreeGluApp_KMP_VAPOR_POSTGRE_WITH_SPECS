package com.ivan.freeglukmp.data.remote

import com.ivan.freeglukmp.domain.model.FoodModel
import com.ivan.freeglukmp.domain.repository.FoodRepository

class FoodRepositoryImpl(private val apiService: ApiService) : FoodRepository {

    override suspend fun getFoods(page: Int, per: Int): Result<List<FoodModel>> {
        return try {
            val response = apiService.getFoods(page, per)
            Result.success(response.items.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchFoods(query: String, page: Int, per: Int): Result<List<FoodModel>> {
        return try {
            val response = apiService.searchFoods(query, page, per)
            Result.success(response.items.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFoodDetail(id: String): Result<FoodModel> {
        return try {
            val dto = apiService.getFoodDetail(id)
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}