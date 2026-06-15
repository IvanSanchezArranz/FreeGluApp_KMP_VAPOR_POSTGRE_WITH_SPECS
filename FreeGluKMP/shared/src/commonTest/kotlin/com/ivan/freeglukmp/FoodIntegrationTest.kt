package com.ivan.freeglukmp

import com.ivan.freeglukmp.data.remote.ApiService
import com.ivan.freeglukmp.data.remote.FoodRepositoryImpl
import com.ivan.freeglukmp.domain.usecase.GetAllFoodsUseCase
import com.ivan.freeglukmp.domain.usecase.GetFoodDetailUseCase
import com.ivan.freeglukmp.domain.usecase.SearchFoodsUseCase
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

class FoodIntegrationTest {

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    private val apiService = ApiService(httpClient)
    private val repository = FoodRepositoryImpl(apiService)
    
    private val getAllFoodsUseCase = GetAllFoodsUseCase(repository)
    private val searchFoodsUseCase = SearchFoodsUseCase(repository)
    private val getFoodDetailUseCase = GetFoodDetailUseCase(repository)

    @Test
    fun testGetFoodsConnectionAndParsing() = runTest {
        try {
            val result = getAllFoodsUseCase(page = 1, per = 10)
            
            assertTrue(result.isSuccess, "Failed to connect to local Vapor backend. Is it running?")
            val foods = result.getOrNull()
            assertNotNull(foods)
            
            println("✅ Successfully connected to Vapor. Retrieved ${foods.size} foods.")
            if (foods.isNotEmpty()) {
                val firstFood = foods[0]
                println("🍏 Sample food: Name=${firstFood.name}, Brand=${firstFood.brand}, IsGlutenFree=${firstFood.isGlutenFree}")
                assertTrue(firstFood.name.isNotEmpty(), "Food name should not be empty")
            }
        } catch (e: Exception) {
            println("❌ Connection failed with exception: ${e.message}")
            throw e
        }
    }

    @Test
    fun testSearchFoodsConnectionAndParsing() = runTest {
        try {
            // Search for something likely to exist, or just perform a query
            val result = searchFoodsUseCase(query = "Gluten", page = 1, per = 10)
            
            assertTrue(result.isSuccess, "Failed to connect to local Vapor search backend.")
            val foods = result.getOrNull()
            assertNotNull(foods)
            
            println("🔍 Search 'Gluten' returned ${foods.size} results.")
            foods.forEach {
                println(" - Found: ${it.name} (isGlutenFree=${it.isGlutenFree})")
            }
        } catch (e: Exception) {
            println("❌ Search connection failed: ${e.message}")
            throw e
        }
    }

    @Test
    fun testGetFoodDetailConnectionAndParsing() = runTest {
        try {
            // First get all foods to retrieve a valid ID
            val listResult = getAllFoodsUseCase(page = 1, per = 5)
            assertTrue(listResult.isSuccess)
            val foods = listResult.getOrNull()
            assertNotNull(foods)
            
            if (foods.isNotEmpty()) {
                val targetId = foods[0].id
                val detailResult = getFoodDetailUseCase(targetId)
                
                assertTrue(detailResult.isSuccess, "Failed to connect to get details for food ID: $targetId")
                val detail = detailResult.getOrNull()
                assertNotNull(detail)
                
                println("📖 Details retrieved for ${detail.name}: Brand=${detail.brand}, Ingredients=${detail.ingredients}")
                assertTrue(detail.ingredients.isNotEmpty(), "Ingredients should not be empty")
            } else {
                println("⚠️ No foods in database to test detail fetching.")
            }
        } catch (e: Exception) {
            println("❌ Detail connection failed: ${e.message}")
            throw e
        }
    }
}