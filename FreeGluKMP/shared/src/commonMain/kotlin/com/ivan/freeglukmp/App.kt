package com.ivan.freeglukmp

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.request.crossfade
import com.ivan.freeglukmp.utils.getCacheDir
import com.ivan.freeglukmp.di.initKoin
import com.ivan.freeglukmp.theme.GlutenFreeTheme
import com.ivan.freeglukmp.presentation.list.FoodsListScreen
import com.ivan.freeglukmp.presentation.list.FoodsListViewModel
import com.ivan.freeglukmp.presentation.detail.FoodDetailScreen
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import kotlinx.coroutines.launch

import com.ivan.freeglukmp.presentation.list.FavoritesScreen

sealed class Screen {
    object Login : Screen()
    object Register : Screen()
    object List : Screen()
    object Favorites : Screen()
    object AddFood : Screen()
    data class EditFood(val id: String) : Screen()
    data class Detail(val id: String) : Screen()
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    // Configure Custom ImageLoader with heavy memory caching + crossfade + disk caching
    setSingletonImageLoaderFactory { context ->
        val cacheDir = getCacheDir(context)
        val builder = ImageLoader.Builder(context)
            .crossfade(true) // Enables smooth crossfading animations!
            .memoryCachePolicy(CachePolicy.ENABLED)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25) // Allocates 25% of available RAM for images cache
                    .strongReferencesEnabled(true)
                    .build()
            }
            
        if (cacheDir != null) {
            builder.diskCachePolicy(CachePolicy.ENABLED)
            builder.diskCache {
                DiskCache.Builder()
                    .directory(cacheDir)
                    .maxSizeBytes(512 * 1024 * 1024) // Allocates up to 512MB on disk
                    .build()
            }
        }
        
        builder.build()
    }

    KoinContext {
        val authRepository: com.ivan.freeglukmp.domain.repository.AuthRepository = koinInject()
        val foodsViewModel: FoodsListViewModel = koinInject()
        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()
        val showToast: (String) -> Unit = { message ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
        val isLoggedIn by authRepository.isLoggedInState.collectAsState()
        var currentScreen by remember {
            mutableStateOf<Screen>(
                if (authRepository.isLoggedIn()) Screen.List else Screen.Login
            )
        }
        var lastMainScreen by remember {
            mutableStateOf<Screen>(
                if (authRepository.isLoggedIn()) Screen.List else Screen.Login
            )
        }

        // Automatically handle global logout / token expiration redirect
        LaunchedEffect(isLoggedIn) {
            if (!isLoggedIn) {
                if (currentScreen !is Screen.Login && currentScreen !is Screen.Register) {
                    currentScreen = Screen.Login
                }
            }
        }

        // Pre-fetch favorites on app startup if logged in
        LaunchedEffect(Unit) {
            if (authRepository.isLoggedIn()) {
                authRepository.fetchAndCacheRemoteFavorites()
            }
        }

        GlutenFreeTheme {
            androidx.compose.material3.Scaffold(
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    if (currentScreen is Screen.List || currentScreen is Screen.Favorites) {
                        NavigationBar {
                            NavigationBarItem(
                                selected = currentScreen is Screen.List,
                                onClick = { 
                                    currentScreen = Screen.List
                                    lastMainScreen = Screen.List
                                },
                                icon = { Icon(imageVector = Icons.Default.List, contentDescription = "Catalog") },
                                label = { Text("Catalog") }
                            )
                            NavigationBarItem(
                                selected = currentScreen is Screen.Favorites,
                                onClick = { 
                                    currentScreen = Screen.Favorites
                                    lastMainScreen = Screen.Favorites
                                },
                                icon = { Icon(imageVector = Icons.Default.Favorite, contentDescription = "Favorites") },
                                label = { Text("Favorites") }
                            )
                        }
                    }
                },
                floatingActionButton = {
                    if (currentScreen is Screen.List && authRepository.isLoggedIn()) {
                        FloatingActionButton(
                            onClick = { currentScreen = Screen.AddFood },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Product"
                            )
                        }
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    when (val screen = currentScreen) {
                        is Screen.Login -> {
                            com.ivan.freeglukmp.presentation.auth.LoginScreen(
                                onNavigateToRegister = { currentScreen = Screen.Register },
                                onLoginSuccess = { 
                                    currentScreen = Screen.List
                                    lastMainScreen = Screen.List
                                },
                                onSkipLogin = { 
                                    currentScreen = Screen.List
                                    lastMainScreen = Screen.List
                                }
                            )
                        }
                        is Screen.Register -> {
                            com.ivan.freeglukmp.presentation.auth.RegisterScreen(
                                onNavigateToLogin = { currentScreen = Screen.Login },
                                onRegisterSuccess = { 
                                    currentScreen = Screen.List
                                    lastMainScreen = Screen.List
                                },
                                onSkipRegister = { 
                                    currentScreen = Screen.List
                                    lastMainScreen = Screen.List
                                }
                            )
                        }
                        is Screen.List -> {
                            FoodsListScreen(
                                onNavigateToDetail = { foodId ->
                                    currentScreen = Screen.Detail(foodId)
                                },
                                onNavigateToAuth = {
                                    currentScreen = Screen.Login
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
                        is Screen.AddFood -> {
                            com.ivan.freeglukmp.presentation.detail.AddEditFoodScreen(
                                foodId = null,
                                onNavigateBack = {
                                    currentScreen = Screen.List
                                },
                                onSaveSuccess = {
                                    foodsViewModel.reloadAndClearFilters()
                                    showToast("Product added successfully! 🌱")
                                    currentScreen = Screen.List
                                }
                            )
                        }
                        is Screen.EditFood -> {
                            com.ivan.freeglukmp.presentation.detail.AddEditFoodScreen(
                                foodId = screen.id,
                                onNavigateBack = {
                                    currentScreen = Screen.Detail(screen.id)
                                },
                                onSaveSuccess = {
                                    foodsViewModel.reloadAndClearFilters()
                                    showToast("Product updated successfully! ✏️")
                                    currentScreen = Screen.Detail(screen.id)
                                }
                            )
                        }
                        is Screen.Detail -> {
                            FoodDetailScreen(
                                foodId = screen.id,
                                onNavigateBack = {
                                    currentScreen = lastMainScreen
                                },
                                onNavigateToEdit = { id ->
                                    currentScreen = Screen.EditFood(id)
                                },
                                onDeleteSuccess = {
                                    foodsViewModel.reloadAndClearFilters()
                                    showToast("Product deleted successfully! 🗑️")
                                    currentScreen = lastMainScreen
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}