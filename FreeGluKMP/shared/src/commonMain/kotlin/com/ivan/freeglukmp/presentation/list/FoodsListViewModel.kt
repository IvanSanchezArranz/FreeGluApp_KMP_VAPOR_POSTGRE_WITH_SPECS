package com.ivan.freeglukmp.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivan.freeglukmp.domain.model.FoodModel
import com.ivan.freeglukmp.domain.usecase.GetAllFoodsUseCase
import com.ivan.freeglukmp.domain.usecase.SearchFoodsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FoodsListViewModel(
    private val getAllFoodsUseCase: GetAllFoodsUseCase,
    private val searchFoodsUseCase: SearchFoodsUseCase
) : ViewModel() {

    private val _foods = MutableStateFlow<List<FoodModel>>(emptyList())
    val foods: StateFlow<List<FoodModel>> = _foods.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isEndOfList = MutableStateFlow(false)
    val isEndOfList: StateFlow<Boolean> = _isEndOfList.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private var currentPage = 1
    private val itemsPerPage = 30
    private var searchJob: Job? = null
    private var loadJob: Job? = null

    init {
        // Initial load
        reload()
    }

    fun onSearchQueryChanged(query: String) {
        if (_searchQuery.value == query) return
        _searchQuery.value = query
        
        // Debounce search input
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (query.isNotBlank()) {
                delay(500L) // 500ms debounce
            }
            reload()
        }
    }

    fun onCategorySelected(category: String) {
        if (_selectedCategory.value == category) return
        _selectedCategory.value = category
        reload()
    }

    fun reload() {
        loadJob?.cancel()
        currentPage = 1
        _isEndOfList.value = false
        _errorMessage.value = null
        _foods.value = emptyList()
        _isLoading.value = true
        
        loadPage(isInitial = true)
    }

    fun loadNextPage() {
        if (_isEndOfList.value || _isLoading.value || _isLoadingMore.value) return
        _isLoadingMore.value = true
        loadPage(isInitial = false)
    }

    private fun loadPage(isInitial: Boolean) {
        loadJob = viewModelScope.launch {
            val query = _searchQuery.value
            val category = _selectedCategory.value

            val result = if (query.isNotBlank()) {
                searchFoodsUseCase(query = query, page = currentPage, per = itemsPerPage)
            } else {
                if (category != "All") {
                    searchFoodsUseCase(query = category, page = currentPage, per = itemsPerPage)
                } else {
                    getAllFoodsUseCase(page = currentPage, per = itemsPerPage)
                }
            }

            result.onSuccess { newItems ->
                // Filter locally by category if text search and a category chip are both active
                val filteredItems = if (query.isNotBlank() && category != "All") {
                    newItems.filter { f ->
                        f.categories.any { it.contains(category, ignoreCase = true) }
                    }
                } else {
                    newItems
                }

                if (isInitial) {
                    _foods.value = filteredItems
                    _isLoading.value = false
                } else {
                    _foods.value = _foods.value + filteredItems
                    _isLoadingMore.value = false
                }

                if (newItems.size < itemsPerPage) {
                    _isEndOfList.value = true
                } else {
                    currentPage++
                }
            }.onFailure { e ->
                _errorMessage.value = e.message ?: "Failed to load foods"
                _isLoading.value = false
                _isLoadingMore.value = false
            }
        }
    }
}