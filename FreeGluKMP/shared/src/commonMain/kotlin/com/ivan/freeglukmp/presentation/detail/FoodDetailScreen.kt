package com.ivan.freeglukmp.presentation.detail

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.ivan.freeglukmp.domain.model.FoodModel
import com.ivan.freeglukmp.domain.usecase.GetFoodDetailUseCase
import com.ivan.freeglukmp.domain.usecase.IsFavoriteUseCase
import com.ivan.freeglukmp.domain.usecase.ToggleFavoriteUseCase
import org.koin.compose.koinInject
import kotlinx.coroutines.launch

@Composable
fun FoodDetailScreen(
    foodId: String,
    onNavigateBack: () -> Unit
) {
    val getFoodDetailUseCase: GetFoodDetailUseCase = koinInject()
    val isFavoriteUseCase: IsFavoriteUseCase = koinInject()
    val toggleFavoriteUseCase: ToggleFavoriteUseCase = koinInject()
    val scope = rememberCoroutineScope()
    
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

    Column(modifier = Modifier.fillMaxSize()) {
        // Compact modern left-aligned custom details header (removes bulky TopAppBar/Scaffold!)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "Product Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            if (food != null) {
                IconButton(
                    onClick = {
                        scope.launch {
                            toggleFavoriteUseCase(foodId)
                        }
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

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (errorMessage != null || food == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(errorMessage ?: "Item not found", color = MaterialTheme.colorScheme.error)
            }
        } else {
            val actualFood = food!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Large Bento-style Image Card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(8.dp)) {
                        SubcomposeAsyncImage(
                            model = actualFood.imageUrl,
                            contentDescription = actualFood.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(20.dp)),
                            loading = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(40.dp),
                                        strokeWidth = 4.dp
                                    )
                                }
                            },
                            error = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = "No Image Available",
                                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                                        modifier = Modifier.size(64.dp)
                                    )
                                }
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                
                // Title and Brand Section
                Text(
                    text = actualFood.name, 
                    style = MaterialTheme.typography.headlineMedium, 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = "By ${actualFood.brand}", 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Custom Gluten Free Premium Banner
                if (actualFood.isGlutenFree) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "🌱 Certified Gluten Free",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Product Details Sections
                Card(
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Categories", 
                            style = MaterialTheme.typography.titleMedium, 
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = actualFood.categories.joinToString(", "), 
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 14.dp),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                        )

                        Text(
                            text = "Ingredients", 
                            style = MaterialTheme.typography.titleMedium, 
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = actualFood.ingredients.ifBlank { "Ingredients list not available." }, 
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
