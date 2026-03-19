package com.jujodevs.cursotestingandroid.productlist.presentation

sealed interface ProductListUiState {
    data object Loading: ProductListUiState
    data class Error(val message: String): ProductListUiState
    data class Success(
        // product: List<>,
        // categories: List <>,
        val selectedCategory: String,
        // sortOption
    ): ProductListUiState
}
