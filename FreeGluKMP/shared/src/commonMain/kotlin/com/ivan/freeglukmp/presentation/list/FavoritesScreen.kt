package com.ivan.freeglukmp.presentation.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ivan.freeglukmp.domain.model.FoodModel
import com.ivan.freeglukmp.domain.usecase.GetFavoriteFoodsUseCase
import com.ivan.freeglukmp.presentation.components.FoodCard
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onNavigateToDetail: (String) -> Unit
) {
    val getFavoriteFoodsUseCase: GetFavoriteFoodsUseCase = koinInject()
    var foods by remember { mutableStateOf<List<FoodModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val result = getFavoriteFoodsUseCase()
        result.onSuccess { 
            foods = it
            isLoading = false
        }.onFailure { 
            errorMessage = it.message ?: "Failed to load favorites"
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Favorites") }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
            }
        } else if (foods.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No favorites saved yet ❤️", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(foods) { food ->
                    FoodCard(
                        food = food,
                        onClick = { onNavigateToDetail(food.id) }
                    )
                }
            }
        }
    }
}