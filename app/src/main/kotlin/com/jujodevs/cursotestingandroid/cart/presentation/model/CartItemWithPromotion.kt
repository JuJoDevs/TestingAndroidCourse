package com.jujodevs.cursotestingandroid.cart.presentation.model

import com.jujodevs.cursotestingandroid.cart.domain.model.CartItem
import com.jujodevs.cursotestingandroid.productlist.domain.model.Product

data class CartItemWithPromotion(
    val cartItem: CartItem,
    val product: Product,
)
