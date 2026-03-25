package com.jujodevs.cursotestingandroid.cart.presentation.model

import com.jujodevs.cursotestingandroid.cart.domain.model.CartItem
import com.jujodevs.cursotestingandroid.productlist.domain.model.ProductWithPromotion

data class CartItemWithPromotion(
    val cartItem: CartItem,
    val item: ProductWithPromotion,
)
