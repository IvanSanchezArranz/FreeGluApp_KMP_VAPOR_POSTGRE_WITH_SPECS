package com.ivan.freeglukmp.domain.usecase

import com.ivan.freeglukmp.data.local.LocalFavoritesDataSource
import com.ivan.freeglukmp.domain.model.FoodModel
import com.ivan.freeglukmp.domain.repository.FoodRepository

class GetAllFoodsUseCase(private val repository: FoodRepository) {
    suspend operator fun invoke(page: Int = 1, per: Int = 20): Result<List<FoodModel>> {
        return repository.getFoods(page, per)
    }
}

class SearchFoodsUseCase(private val repository: FoodRepository) {
    suspend operator fun invoke(query: String, page: Int = 1, per: Int = 20): Result<List<FoodModel>> {
        if (query.isBlank()) return repository.getFoods(page, per)
        return repository.searchFoods(query, page, per)
    }
}

class GetFoodDetailUseCase(private val repository: FoodRepository) {
    suspend operator fun invoke(id: String): Result<FoodModel> {
        return repository.getFoodDetail(id)
    }
}

class ToggleFavoriteUseCase(private val dataSource: LocalFavoritesDataSource) {
    operator fun invoke(id: String) {
        dataSource.toggleFavorite(id)
    }
}

class IsFavoriteUseCase(private val dataSource: LocalFavoritesDataSource) {
    operator fun invoke(id: String): Boolean {
        return dataSource.isFavorite(id)
    }
}

class GetFavoriteFoodsUseCase(
    private val dataSource: LocalFavoritesDataSource,
    private val repository: FoodRepository
) {
    suspend operator fun invoke(): Result<List<FoodModel>> {
        return try {
            val favoriteIds = dataSource.getAllFavorites()
            val favoriteFoods = mutableListOf<FoodModel>()
            for (id in favoriteIds) {
                repository.getFoodDetail(id).onSuccess {
                    favoriteFoods.add(it)
                }
            }
            Result.success(favoriteFoods)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}