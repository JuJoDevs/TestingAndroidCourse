package com.jujodevs.cursotestingandroid.productlist.domain.model

import java.time.Instant

enum class PromotionType {
    PERCENT,
    BUY_X_PAY_Y,
}

data class Promotion(
    val id: String,
    val type: PromotionType,
    val productsIds: List<String>,
    val value: Double,
    val buyQuantity: Int? = null,
    val startTime: Instant,
    val endTime: Instant,
)
