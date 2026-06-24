package com.ivan.freeglukmp.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivan.freeglukmp.domain.model.FoodModel
import com.ivan.freeglukmp.domain.usecase.CreateFoodUseCase
import com.ivan.freeglukmp.domain.usecase.GetFoodDetailUseCase
import com.ivan.freeglukmp.domain.usecase.UpdateFoodUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AddEditFoodState {
    object Idle : AddEditFoodState
    object Loading : AddEditFoodState
    data class Success(val food: FoodModel) : AddEditFoodState
    data class Error(val message: String) : AddEditFoodState
}

class AddEditFoodViewModel(
    private val getFoodDetailUseCase: GetFoodDetailUseCase,
    private val createFoodUseCase: CreateFoodUseCase,
    private val updateFoodUseCase: UpdateFoodUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<AddEditFoodState>(AddEditFoodState.Idle)
    val state: StateFlow<AddEditFoodState> = _state.asStateFlow()

    // Form fields
    var code = MutableStateFlow("")
    var name = MutableStateFlow("")
    var brand = MutableStateFlow("")
    var categories = MutableStateFlow("")
    var ingredients = MutableStateFlow("")
    var imageUrl = MutableStateFlow("")
    var isGlutenFree = MutableStateFlow(true)

    fun loadFoodForEditing(id: String) {
        _state.value = AddEditFoodState.Loading
        viewModelScope.launch {
            getFoodDetailUseCase(id).onSuccess { food ->
                code.value = food.code
                name.value = food.name
                brand.value = food.brand
                categories.value = food.categories.joinToString(",")
                ingredients.value = food.ingredients
                imageUrl.value = food.imageUrl
                isGlutenFree.value = food.isGlutenFree
                _state.value = AddEditFoodState.Idle
            }.onFailure { e ->
                _state.value = AddEditFoodState.Error(e.message ?: "Failed to load product details")
            }
        }
    }

    fun saveProduct(id: String? = null) {
        val currentCode = code.value.trim()
        val currentName = name.value.trim()
        val currentBrand = brand.value.trim()
        val currentCategories = categories.value.trim()
        val currentIngredients = ingredients.value.trim()
        val currentImageUrl = imageUrl.value.trim()

        // 1. Detailed Barcode Validation (EAN/UPC standard)
        if (currentCode.isBlank()) {
            _state.value = AddEditFoodState.Error("Barcode / Product Code is required and cannot be empty.")
            return
        }
        if (!currentCode.all { it.isDigit() }) {
            _state.value = AddEditFoodState.Error("Invalid Barcode format. It must contain only numeric digits (0-9). Spaces, letters, or symbols are not allowed.")
            return
        }
        if (currentCode.length < 8 || currentCode.length > 13) {
            _state.value = AddEditFoodState.Error("Invalid Barcode length. It must be a standard product barcode of 8 to 13 digits (EAN/UPC standard). Entered length: ${currentCode.length}.")
            return
        }

        // 2. Detailed Name Validation
        if (currentName.isBlank()) {
            _state.value = AddEditFoodState.Error("Product Name is required and cannot be empty.")
            return
        }
        if (currentName.length < 3) {
            _state.value = AddEditFoodState.Error("Product Name is too short. It must be at least 3 characters long to ensure identification.")
            return
        }

        // 3. Detailed Brand Validation
        if (currentBrand.isBlank()) {
            _state.value = AddEditFoodState.Error("Brand Name is required and cannot be empty.")
            return
        }

        // 4. Detailed Categories Validation
        if (currentCategories.isBlank()) {
            _state.value = AddEditFoodState.Error("At least one Category is required (e.g. 'Bread' or 'Pasta').")
            return
        }
        val categoriesList = currentCategories.split(",").map { it.trim() }
        if (categoriesList.any { it.isBlank() }) {
            _state.value = AddEditFoodState.Error("Invalid Categories format. Ensure categories are separated by commas and contain no empty items. Example of valid format: 'Bread, Pasta, Snacks'.")
            return
        }

        // 5. Detailed Image URL Validation
        if (currentImageUrl.isNotEmpty()) {
            if (!currentImageUrl.startsWith("http://") && !currentImageUrl.startsWith("https://")) {
                _state.value = AddEditFoodState.Error("Invalid Image URL format. It must start with 'http://' or 'https://' protocol prefixes.")
                return
            }
        }

        _state.value = AddEditFoodState.Loading
        viewModelScope.launch {
            println("[KMP CRUD] Saving product. Received ID parameter: '$id'")
            val foodModel = FoodModel(
                id = id ?: "",
                code = currentCode,
                name = currentName,
                brand = currentBrand,
                categories = categoriesList,
                ingredients = currentIngredients,
                imageUrl = currentImageUrl,
                isGlutenFree = isGlutenFree.value
            )

            val result = if (id != null) {
                println("[KMP CRUD] Calling updateFoodUseCase for ID: '$id'")
                updateFoodUseCase(id, foodModel)
            } else {
                println("[KMP CRUD] Calling createFoodUseCase (New Product)")
                createFoodUseCase(foodModel)
            }

            result.onSuccess { savedFood ->
                _state.value = AddEditFoodState.Success(savedFood)
            }.onFailure { e ->
                _state.value = AddEditFoodState.Error(e.message ?: "Failed to save product")
            }
        }
    }
}
