package com.jujodevs.cursotestingandroid.detail.presentation

import com.jujodevs.cursotestingandroid.productlist.domain.model.ProductWithPromotion

data class ProductDetailUiState(
    val item: ProductWithPromotion? = null,
    val isLoading: Boolean = true,
)
