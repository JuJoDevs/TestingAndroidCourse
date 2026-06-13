package com.jujodevs.cursotestingandroid.detail.presentation

sealed interface ProductDetailAction {
    data object GoBack : ProductDetailAction

    data object AddToCart : ProductDetailAction
}
