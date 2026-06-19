package com.jujodevs.cursotestingandroid.cart.domain.usecase

import com.jujodevs.cursotestingandroid.cart.domain.repository.CartRepository
import com.jujodevs.cursotestingandroid.core.domain.model.AppError
import com.jujodevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class UpdateCartItemUseCase
    @Inject
    constructor(
        private val cartRepository: CartRepository,
        private val productRepository: ProductRepository,
    ) {
        suspend operator fun invoke(
            productId: String,
            quantity: Int,
        ) {
            if (quantity < 0) throw AppError.Validation.QuantityMustBePositive

            if (quantity == 0) {
                cartRepository.removeFromCart(productId)
                return
            }

            val product =
                productRepository.getProductById(productId).firstOrNull()
                    ?: throw AppError.NotFoundError

            if (quantity > product.stock) throw AppError.Validation.InsufficientStock(product.stock)

            cartRepository.updateQuantity(productId, quantity)
        }
    }
