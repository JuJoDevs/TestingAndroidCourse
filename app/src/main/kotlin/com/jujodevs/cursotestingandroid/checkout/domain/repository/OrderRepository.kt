package com.jujodevs.cursotestingandroid.checkout.domain.repository

import com.jujodevs.cursotestingandroid.checkout.domain.model.OrderConfirmation

interface OrderRepository {

    suspend fun placeOrder(): OrderConfirmation
}
