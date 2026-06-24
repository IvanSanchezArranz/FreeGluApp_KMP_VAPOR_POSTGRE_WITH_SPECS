package com.ivan.freeglukmp.data.remote

import com.ivan.freeglukmp.data.local.TokenStorage
import com.ivan.freeglukmp.domain.model.FoodModel
import com.ivan.freeglukmp.domain.repository.AuthRepository
import com.ivan.freeglukmp.getApiBaseUrl
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepositoryImpl(
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage
) : AuthRepository {

    private val baseUrl = getApiBaseUrl()

    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    override val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    private val _isLoggedInState = MutableStateFlow(tokenStorage.getToken() != null)
    override val isLoggedInState: StateFlow<Boolean> = _isLoggedInState.asStateFlow()

    init {
        // Pre-fetch favorites if already logged in on startup
        if (isLoggedIn()) {
            // Launch in background/coroutine or pre-fetch on demand. We expose the fetcher method.
        }
    }

    override suspend fun register(email: String, password: String): Result<UserResponseDTO> {
        return try {
            val response: AuthResponseDTO = httpClient.post("$baseUrl/register") {
                contentType(ContentType.Application.Json)
                setBody(AuthRequestDTO(email, password))
            }.body()
            
            tokenStorage.saveToken(response.token)
            _isLoggedInState.value = true
            fetchAndCacheRemoteFavorites()
            Result.success(response.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, password: String): Result<UserResponseDTO> {
        return try {
            val response: AuthResponseDTO = httpClient.post("$baseUrl/login") {
                contentType(ContentType.Application.Json)
                setBody(AuthRequestDTO(email, password))
            }.body()
            
            tokenStorage.saveToken(response.token)
            _isLoggedInState.value = true
            fetchAndCacheRemoteFavorites()
            Result.success(response.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getSavedToken(): String? {
        return tokenStorage.getToken()
    }

    override fun logout() {
        tokenStorage.clearToken()
        _favoriteIds.value = emptySet()
        _isLoggedInState.value = false
    }

    override fun isLoggedIn(): Boolean {
        return tokenStorage.getToken() != null
    }

    override suspend fun syncFavorites(foodIds: List<String>): Result<Int> {
        val token = tokenStorage.getToken() ?: return Result.failure(Exception("Not logged in"))
        return try {
            val response: SyncResponseDTO = httpClient.post("$baseUrl/favorites/sync") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(SyncFavoritesDTO(foodIds))
            }.body()
            fetchAndCacheRemoteFavorites()
            Result.success(response.syncedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRemoteFavorites(): Result<List<FoodModel>> {
        val token = tokenStorage.getToken() ?: return Result.failure(Exception("Not logged in"))
        return try {
            val dtos: List<FoodDTO> = httpClient.get("$baseUrl/favorites") {
                header("Authorization", "Bearer $token")
            }.body()
            
            val models = dtos.map { it.toDomain() }
            // Keep the cache populated
            _favoriteIds.value = models.map { it.id }.toSet()
            Result.success(models)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchAndCacheRemoteFavorites(): Result<Unit> {
        val token = tokenStorage.getToken() ?: return Result.failure(Exception("Not logged in"))
        return try {
            val dtos: List<FoodDTO> = httpClient.get("$baseUrl/favorites") {
                header("Authorization", "Bearer $token")
            }.body()
            _favoriteIds.value = dtos.mapNotNull { it.id }.toSet()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addRemoteFavorite(foodId: String): Result<Unit> {
        val token = tokenStorage.getToken() ?: return Result.failure(Exception("Not logged in"))
        val previousFavorites = _favoriteIds.value
        _favoriteIds.value = previousFavorites + foodId
        
        return try {
            httpClient.post("$baseUrl/favorites/$foodId") {
                header("Authorization", "Bearer $token")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            _favoriteIds.value = previousFavorites
            Result.failure(e)
        }
    }

    override suspend fun removeRemoteFavorite(foodId: String): Result<Unit> {
        val token = tokenStorage.getToken() ?: return Result.failure(Exception("Not logged in"))
        val previousFavorites = _favoriteIds.value
        _favoriteIds.value = previousFavorites - foodId
        
        return try {
            httpClient.delete("$baseUrl/favorites/$foodId") {
                header("Authorization", "Bearer $token")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            _favoriteIds.value = previousFavorites
            Result.failure(e)
        }
    }
}
