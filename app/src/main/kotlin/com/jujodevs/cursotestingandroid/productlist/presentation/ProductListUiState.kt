package com.jujodevs.cursotestingandroid.productlist.presentation

import com.jujodevs.cursotestingandroid.productlist.domain.model.Product
import com.jujodevs.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import com.jujodevs.cursotestingandroid.productlist.domain.model.SortOption

sealed interface ProductListUiState {
    data object Loading: ProductListUiState
    data class Error(val message: String): ProductListUiState
    data class Success(
        val products: List<ProductWithPromotion>,
        val categories: List <String>,
        val selectedCategory: String?,
        val sortOption: SortOption,
    ): ProductListUiState
}
