package com.ivan.freeglukmp.di

import com.ivan.freeglukmp.data.local.LocalFavoritesDataSource
import com.ivan.freeglukmp.data.remote.ApiService
import com.ivan.freeglukmp.data.remote.FoodRepositoryImpl
import com.ivan.freeglukmp.domain.repository.FoodRepository
import com.ivan.freeglukmp.domain.usecase.GetAllFoodsUseCase
import com.ivan.freeglukmp.domain.usecase.GetFoodDetailUseCase
import com.ivan.freeglukmp.domain.usecase.SearchFoodsUseCase
import com.ivan.freeglukmp.domain.usecase.ToggleFavoriteUseCase
import com.ivan.freeglukmp.domain.usecase.IsFavoriteUseCase
import com.ivan.freeglukmp.domain.usecase.GetFavoriteFoodsUseCase
import com.ivan.freeglukmp.presentation.list.FoodsListViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

val sharedModule = module {
    single {
        HttpClient {
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
    single<FoodRepository> { FoodRepositoryImpl(get()) }
    single { LocalFavoritesDataSource() }
    
    factory { GetAllFoodsUseCase(get()) }
    factory { SearchFoodsUseCase(get()) }
    factory { GetFoodDetailUseCase(get()) }
    factory { ToggleFavoriteUseCase(get()) }
    factory { IsFavoriteUseCase(get()) }
    factory { GetFavoriteFoodsUseCase(get(), get()) }
    factory { FoodsListViewModel(get(), get()) }
}

fun initKoin(appModule: Module = module { }) {
    org.koin.core.context.startKoin {
        modules(sharedModule, appModule)
    }
}