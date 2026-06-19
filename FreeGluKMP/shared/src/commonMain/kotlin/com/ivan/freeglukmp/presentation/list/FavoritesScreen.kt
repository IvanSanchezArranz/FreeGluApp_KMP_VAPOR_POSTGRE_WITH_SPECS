package com.ivan.freeglukmp.presentation.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ivan.freeglukmp.domain.model.FoodModel
import com.ivan.freeglukmp.domain.usecase.GetFavoriteFoodsUseCase
import com.ivan.freeglukmp.presentation.components.FoodCard
import org.koin.compose.koinInject

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

    Column(modifier = Modifier.fillMaxSize()) {
        // Compact modern left-aligned custom header (matching FoodsListScreen perfectly!)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "My Favorites",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
            }
        } else if (foods.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No favorites saved yet ❤️", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2), // Symmetrical standard 2-column mobile grid (matching catalog perfectly)
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
