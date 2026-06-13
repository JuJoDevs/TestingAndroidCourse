package com.jujodevs.cursotestingandroid.detail.presentation

sealed interface ProductDetailEvent {
    data object GoBack : ProductDetailEvent

    data object UnknownError : ProductDetailEvent

    data object NetworkError : ProductDetailEvent

    data object InsufficientStockError : ProductDetailEvent

    data object SuccessAddToCart : ProductDetailEvent
}
