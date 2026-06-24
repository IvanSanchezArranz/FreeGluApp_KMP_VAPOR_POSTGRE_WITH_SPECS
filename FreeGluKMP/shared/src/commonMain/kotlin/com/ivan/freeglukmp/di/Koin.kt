package com.ivan.freeglukmp.di

import com.ivan.freeglukmp.data.local.LocalFavoritesDataSource
import com.ivan.freeglukmp.data.local.TokenStorage
import com.ivan.freeglukmp.data.remote.ApiService
import com.ivan.freeglukmp.data.remote.FoodRepositoryImpl
import com.ivan.freeglukmp.domain.repository.FoodRepository
import com.ivan.freeglukmp.domain.usecase.*
import com.ivan.freeglukmp.presentation.list.*
import com.ivan.freeglukmp.presentation.detail.*
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

val sharedModule = module {
    single {
        HttpClient {
            expectSuccess = true
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }
    }
    
    single { ApiService(get()) }
    single<FoodRepository> { FoodRepositoryImpl(get(), get()) }
    single { LocalFavoritesDataSource() }
    single { TokenStorage() }
    single<com.ivan.freeglukmp.domain.repository.AuthRepository> { com.ivan.freeglukmp.data.remote.AuthRepositoryImpl(get(), get()) }
    
    factory { GetAllFoodsUseCase(get()) }
    factory { SearchFoodsUseCase(get()) }
    factory { GetFoodDetailUseCase(get()) }
    factory { ToggleFavoriteUseCase(get(), get()) }
    factory { IsFavoriteUseCase(get(), get()) }
    factory { GetFavoriteFoodsUseCase(get(), get(), get()) }
    factory { CreateFoodUseCase(get()) }
    factory { UpdateFoodUseCase(get()) }
    factory { DeleteFoodUseCase(get()) }
    factory { AddEditFoodViewModel(get(), get(), get()) }
    single { FoodsListViewModel(get(), get()) }
}

fun initKoin(appModule: Module = module { }) {
    org.koin.core.context.startKoin {
        modules(sharedModule, appModule)
    }
}