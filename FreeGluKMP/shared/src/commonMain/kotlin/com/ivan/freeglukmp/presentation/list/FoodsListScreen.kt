package com.ivan.freeglukmp.presentation.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ivan.freeglukmp.domain.model.FoodModel
import com.ivan.freeglukmp.domain.usecase.GetAllFoodsUseCase
import com.ivan.freeglukmp.domain.usecase.SearchFoodsUseCase
import com.ivan.freeglukmp.presentation.components.FoodCard
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodsListScreen(
    onNavigateToDetail: (String) -> Unit
) {
    val getAllFoodsUseCase: GetAllFoodsUseCase = koinInject()
    val searchFoodsUseCase: SearchFoodsUseCase = koinInject()
    
    var searchQuery by remember { mutableStateOf("") }
    val categories = listOf("All", "Bread", "Pasta", "Snacks", "Cookies", "Fruits")
    var selectedCategory by remember { mutableStateOf("All") }
    
    var foods by remember { mutableStateOf<List<FoodModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(searchQuery, selectedCategory) {
        isLoading = true
        errorMessage = null
        
        if (searchQuery.isNotBlank()) {
            delay(500L) // debounce
            val result = searchFoodsUseCase(query = searchQuery, page = 1, per = 50)
            result.onSuccess { list ->
                foods = if (selectedCategory != "All") {
                    list.filter { f ->
                        f.categories.any { it.contains(selectedCategory, ignoreCase = true) }
                    }
                } else {
                    list
                }
                isLoading = false
            }.onFailure {
                errorMessage = it.message ?: "Failed to perform search"
                isLoading = false
            }
        } else {
            // Load base list, optionally filtered or queried by category
            val result = if (selectedCategory != "All") {
                searchFoodsUseCase(query = selectedCategory, page = 1, per = 50)
            } else {
                getAllFoodsUseCase(page = 1, per = 50)
            }
            result.onSuccess {
                foods = it
                isLoading = false
            }.onFailure {
                errorMessage = it.message ?: "Failed to load foods"
                isLoading = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = { Text("Gluten Free Catalog") }
        )
        
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search food, category, brand...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = true
        )
        
        // Categories Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = category == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = category },
                    label = { Text(category) }
                )
            }
        }
        
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
            }
        } else if (foods.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No products found matching filters", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                modifier = Modifier.fillMaxSize(),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CenterAlignedTopAppBar(title: @Composable () -> Unit) {
    TopAppBar(title = title)
}