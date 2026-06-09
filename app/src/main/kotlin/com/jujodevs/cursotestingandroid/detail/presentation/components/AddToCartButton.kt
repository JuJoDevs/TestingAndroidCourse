package com.jujodevs.cursotestingandroid.detail.presentation.components

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jujodevs.cursotestingandroid.productlist.domain.model.Product

@Composable
fun AddToCartButton(
    product: Product?,
    isLoading: Boolean,
    addToCart: () -> Unit,
    modifier: Modifier = Modifier,
    ) {
    Surface(
        modifier = modifier,
    ) {
        product?.let { product ->
            if (product.stock > 0) {
                AddToCartButtonWithStock(
                    isLoading = isLoading,
                    addToCart = addToCart,
                )
            } else {
                AddToCartButtonNoStock()
            }
        }
    }
}