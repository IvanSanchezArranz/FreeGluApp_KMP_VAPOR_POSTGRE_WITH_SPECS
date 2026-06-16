package com.ivan.freeglukmp.presentation.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ivan.freeglukmp.domain.model.FoodModel
import com.ivan.freeglukmp.domain.usecase.GetFoodDetailUseCase
import com.ivan.freeglukmp.domain.usecase.IsFavoriteUseCase
import com.ivan.freeglukmp.domain.usecase.ToggleFavoriteUseCase
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailScreen(
    foodId: String,
    onNavigateBack: () -> Unit
) {
    val getFoodDetailUseCase: GetFoodDetailUseCase = koinInject()
    val isFavoriteUseCase: IsFavoriteUseCase = koinInject()
    val toggleFavoriteUseCase: ToggleFavoriteUseCase = koinInject()
    
    var food by remember { mutableStateOf<FoodModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isFavorite by remember { mutableStateOf(false) }

    LaunchedEffect(foodId) {
        val result = getFoodDetailUseCase(foodId)
        result.onSuccess { 
            food = it
            isFavorite = isFavoriteUseCase(foodId)
            isLoading = false
        }.onFailure { 
            errorMessage = it.message ?: "Failed to load detail"
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (food != null) {
                        IconButton(
                            onClick = {
                                toggleFavoriteUseCase(foodId)
                                isFavorite = !isFavorite
                            }
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (isFavorite) "Remove Favorite" else "Add Favorite",
                                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null || food == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(errorMessage ?: "Item not found", color = MaterialTheme.colorScheme.error)
            }
        } else {
            val actualFood = food!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                AsyncImage(
                    model = actualFood.imageUrl,
                    contentDescription = actualFood.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(text = actualFood.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Brand: ${actualFood.brand}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Categories", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(actualFood.categories.joinToString(", "), style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))

                Text("Ingredients", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(actualFood.ingredients, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))

                if (actualFood.isGlutenFree) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text(
                            text = "✅ Certified Gluten Free",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}