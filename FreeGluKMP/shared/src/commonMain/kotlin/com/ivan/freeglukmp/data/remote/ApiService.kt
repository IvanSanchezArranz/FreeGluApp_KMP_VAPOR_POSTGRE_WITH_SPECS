package com.ivan.freeglukmp.data.remote

import com.ivan.freeglukmp.getApiBaseUrl
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class ApiService(private val httpClient: HttpClient) {
    
    private val baseUrl = getApiBaseUrl()

    suspend fun getFoods(page: Int, per: Int): PaginatedResponseDTO<FoodDTO> {
        return httpClient.get("$baseUrl/foods") {
            parameter("page", page)
            parameter("per", per)
        }.body()
    }

    suspend fun searchFoods(query: String, page: Int, per: Int): PaginatedResponseDTO<FoodDTO> {
        return httpClient.get("$baseUrl/foods/search") {
            parameter("q", query)
            parameter("page", page)
            parameter("per", per)
        }.body()
    }

    suspend fun getFoodDetail(id: String): FoodDTO {
        return httpClient.get("$baseUrl/foods/$id").body()
    }
}