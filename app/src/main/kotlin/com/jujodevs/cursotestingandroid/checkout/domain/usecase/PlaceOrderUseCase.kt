package com.jujodevs.cursotestingandroid.checkout.domain.usecase

import com.jujodevs.cursotestingandroid.cart.domain.repository.CartRepository
import com.jujodevs.cursotestingandroid.checkout.domain.model.OrderConfirmation
import com.jujodevs.cursotestingandroid.checkout.domain.repository.OrderRepository
import javax.inject.Inject

class PlaceOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
    private val cartRepository: CartRepository,
) {

    suspend operator fun invoke(): Result<OrderConfirmation> =
        orderRepository.placeOrder()
            .onSuccess {
                cartRepository.clearCart()
            }
}
