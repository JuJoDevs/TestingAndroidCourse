package com.jujodevs.cursotestingandroid.productlist.presentation

import com.jujodevs.cursotestingandroid.productlist.domain.model.Product

sealed interface ProductListUiState {
    data object Loading: ProductListUiState
    data class Error(val message: String): ProductListUiState
    data class Success(
        val products: List<Product>,
        // categories: List <>,
        // val selectedCategory: String,
        // sortOption
    ): ProductListUiState
}
