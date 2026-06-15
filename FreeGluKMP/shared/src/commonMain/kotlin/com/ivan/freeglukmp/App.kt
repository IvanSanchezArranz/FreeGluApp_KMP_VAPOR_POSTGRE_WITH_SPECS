package com.ivan.freeglukmp

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import com.ivan.freeglukmp.di.initKoin
import com.ivan.freeglukmp.theme.GlutenFreeTheme
import com.ivan.freeglukmp.presentation.list.FoodsListScreen
import com.ivan.freeglukmp.presentation.detail.FoodDetailScreen

import com.ivan.freeglukmp.presentation.list.FavoritesScreen

sealed class Screen {
    object List : Screen()
    object Favorites : Screen()
    data class Detail(val id: String) : Screen()
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    // Configure Custom ImageLoader with heavy memory caching for super fast loading of images
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25) // Allocates 25% of available RAM for images cache
                    .strongReferencesEnabled(true)
                    .build()
            }
            .build()
    }

    var currentScreen by remember { mutableStateOf<Screen>(Screen.List) }
    
    GlutenFreeTheme {
        androidx.compose.material3.Scaffold(
            bottomBar = {
                if (currentScreen is Screen.List || currentScreen is Screen.Favorites) {
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentScreen is Screen.List,
                            onClick = { currentScreen = Screen.List },
                            icon = { Text("🟢") },
                            label = { Text("Catalog") }
                        )
                        NavigationBarItem(
                            selected = currentScreen is Screen.Favorites,
                            onClick = { currentScreen = Screen.Favorites },
                            icon = { Text("❤️") },
                            label = { Text("Favorites") }
                        )
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when (val screen = currentScreen) {
                    is Screen.List -> {
                        FoodsListScreen(
                            onNavigateToDetail = { foodId ->
                                currentScreen = Screen.Detail(foodId)
                            }
                        )
                    }
                    is Screen.Favorites -> {
                        FavoritesScreen(
                            onNavigateToDetail = { foodId ->
                                currentScreen = Screen.Detail(foodId)
                            }
                        )
                    }
                    is Screen.Detail -> {
                        FoodDetailScreen(
                            foodId = screen.id,
                            onNavigateBack = {
                                currentScreen = Screen.List
                            }
                        )
                    }
                }
            }
        }
    }
}