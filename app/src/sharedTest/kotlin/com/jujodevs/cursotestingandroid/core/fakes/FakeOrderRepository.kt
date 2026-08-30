package com.jujodevs.cursotestingandroid.core.fakes

import com.jujodevs.cursotestingandroid.checkout.domain.model.OrderConfirmation
import com.jujodevs.cursotestingandroid.checkout.domain.repository.OrderRepository

class FakeOrderRepository(
    private val result: Result<OrderConfirmation> =
        Result.success(
            OrderConfirmation(
                orderId = "order-1",
                etaMinutes = 30,
                total = 25.0,
            ),
        ),
) : OrderRepository {
    override suspend fun placeOrder(): Result<OrderConfirmation> = result
}
