package com.jujodevs.cursotestingandroid.core.mothers.uistate

import com.jujodevs.cursotestingandroid.cart.domain.model.CartItem
import com.jujodevs.cursotestingandroid.cart.domain.model.CartSummary
import com.jujodevs.cursotestingandroid.cart.presentation.CartUiState
import com.jujodevs.cursotestingandroid.cart.presentation.model.CartItemWithPromotion
import com.jujodevs.cursotestingandroid.core.mothers.ProductMother.bread
import com.jujodevs.cursotestingandroid.core.mothers.ProductMother.coffee
import com.jujodevs.cursotestingandroid.core.mothers.PromotionMother.percent
import com.jujodevs.cursotestingandroid.productlist.domain.model.Product
import com.jujodevs.cursotestingandroid.productlist.domain.model.ProductPromotion
import com.jujodevs.cursotestingandroid.productlist.domain.model.ProductWithPromotion

object CartUiStateMother {
    val cartSuccess =
        CartUiState.Success(
            summary =
                CartSummary(
                    subtotal = 10.3,
                    discountTotal = 0.7,
                    finalTotal = 11.0,
                ),
            cartItems =
                listOf(
                    cartItemWithPromotion(product = bread, quantity = 2),
                    cartItemWithPromotion(product = coffee, quantity = 1, promotion = percent),
                ),
            isLoading = false,
        )

    fun cartItemWithPromotion(
        product: Product,
        quantity: Int,
        promotion: ProductPromotion? = null,
    ) = CartItemWithPromotion(
        cartItem =
            CartItem(
                productId = product.id,
                quantity = quantity,
            ),
        item =
            ProductWithPromotion(
                product = product,
                promotion = promotion,
            ),
    )
}
