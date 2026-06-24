package com.ivan.freeglukmp.data.remote

import com.ivan.freeglukmp.getApiBaseUrl
import com.ivan.freeglukmp.getCurrentTimeMillis
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ApiService(private val httpClient: HttpClient) {
    
    private val baseUrl = getApiBaseUrl()

    suspend fun getFoods(page: Int, per: Int): PaginatedResponseDTO<FoodDTO> {
        return httpClient.get("$baseUrl/foods") {
            parameter("page", page)
            parameter("per", per)
            parameter("_t", getCurrentTimeMillis())
        }.body()
    }

    suspend fun searchFoods(query: String, page: Int, per: Int): PaginatedResponseDTO<FoodDTO> {
        return httpClient.get("$baseUrl/foods/search") {
            parameter("q", query)
            parameter("page", page)
            parameter("per", per)
            parameter("_t", getCurrentTimeMillis())
        }.body()
    }

    suspend fun getFoodDetail(id: String): FoodDTO {
        return httpClient.get("$baseUrl/foods/$id") {
            parameter("_t", getCurrentTimeMillis())
        }.body()
    }

    suspend fun createFood(token: String, food: FoodRequestDTO): FoodDTO {
        return httpClient.post("$baseUrl/foods") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(food)
        }.body()
    }

    suspend fun updateFood(token: String, id: String, food: FoodRequestDTO): FoodDTO {
        return httpClient.put("$baseUrl/foods/$id") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(food)
        }.body()
    }

    suspend fun deleteFood(token: String, id: String) {
        httpClient.delete("$baseUrl/foods/$id") {
            header("Authorization", "Bearer $token")
        }
    }
}