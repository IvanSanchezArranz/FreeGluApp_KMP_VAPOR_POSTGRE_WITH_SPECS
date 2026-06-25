package com.ivan.freeglukmp.data.remote

import com.ivan.freeglukmp.domain.model.FoodModel
import com.ivan.freeglukmp.domain.repository.FoodRepository
import com.ivan.freeglukmp.domain.repository.AuthRepository
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode

class FoodRepositoryImpl(
    private val apiService: ApiService,
    private val authRepository: AuthRepository
) : FoodRepository {

    override suspend fun getFoods(page: Int, per: Int): Result<List<FoodModel>> {
        return try {
            val token = authRepository.getSavedToken()
            val response = apiService.getFoods(token, page, per)
            Result.success(response.items.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchFoods(query: String, page: Int, per: Int): Result<List<FoodModel>> {
        return try {
            val token = authRepository.getSavedToken()
            val response = apiService.searchFoods(token, query, page, per)
            Result.success(response.items.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFoodDetail(id: String): Result<FoodModel> {
        return try {
            val token = authRepository.getSavedToken()
            val dto = apiService.getFoodDetail(token, id)
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createFood(food: FoodModel): Result<FoodModel> {
        return try {
            val token = authRepository.getSavedToken() ?: throw Exception("Unauthorized: Token missing")
            val request = FoodRequestDTO(
                code = food.code,
                name = food.name,
                brand = food.brand,
                categories = food.categories.joinToString(","),
                ingredients = food.ingredients,
                imageUrl = food.imageUrl,
                countries = "",
                glutenFree = food.isGlutenFree
            )
            val response = apiService.createFood(token, request)
            Result.success(response.toDomain())
        } catch (e: ResponseException) {
            if (e.response.status == HttpStatusCode.Unauthorized) {
                authRepository.logout()
                Result.failure(Exception("Your session has expired. Please log out and log in again to continue."))
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateFood(id: String, food: FoodModel): Result<FoodModel> {
        return try {
            val token = authRepository.getSavedToken() ?: throw Exception("Unauthorized: Token missing")
            val request = FoodRequestDTO(
                code = food.code,
                name = food.name,
                brand = food.brand,
                categories = food.categories.joinToString(","),
                ingredients = food.ingredients,
                imageUrl = food.imageUrl,
                countries = "",
                glutenFree = food.isGlutenFree
            )
            val response = apiService.updateFood(token, id, request)
            Result.success(response.toDomain())
        } catch (e: ResponseException) {
            if (e.response.status == HttpStatusCode.Unauthorized) {
                authRepository.logout()
                Result.failure(Exception("Your session has expired. Please log out and log in again to continue."))
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteFood(id: String): Result<Unit> {
        return try {
            val token = authRepository.getSavedToken() ?: throw Exception("Unauthorized: Token missing")
            apiService.deleteFood(token, id)
            Result.success(Unit)
        } catch (e: ResponseException) {
            if (e.response.status == HttpStatusCode.Unauthorized) {
                authRepository.logout()
                Result.failure(Exception("Your session has expired. Please log out and log in again to continue."))
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}