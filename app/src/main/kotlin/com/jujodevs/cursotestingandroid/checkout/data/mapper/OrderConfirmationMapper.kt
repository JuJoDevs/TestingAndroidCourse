package com.jujodevs.cursotestingandroid.checkout.data.mapper

import com.jujodevs.cursotestingandroid.checkout.data.remote.response.OrderConfirmationResponse
import com.jujodevs.cursotestingandroid.checkout.domain.model.OrderConfirmation

fun OrderConfirmationResponse.toDomain(): OrderConfirmation =
    OrderConfirmation(
        orderId = orderId,
        etaMinutes = etaMinutes,
        total = total,
    )
