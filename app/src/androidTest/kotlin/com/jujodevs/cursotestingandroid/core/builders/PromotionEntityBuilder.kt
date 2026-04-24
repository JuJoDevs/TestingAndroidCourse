package com.jujodevs.cursotestingandroid.core.builders

import com.jujodevs.cursotestingandroid.core.toListString
import com.jujodevs.cursotestingandroid.productlist.data.local.database.entity.PromotionEntity
import com.jujodevs.cursotestingandroid.productlist.domain.model.Promotion
import com.jujodevs.cursotestingandroid.productlist.domain.model.PromotionType
import java.time.Instant

class PromotionEntityBuilder {
    private var id: String = "promotion-1"
    private var type: String = PromotionType.PERCENT.name
    private var productIds: String = """["productId1"]"""
    private var value: Double = 10.0
    private var buyQuantity: Int? = null
    private var startAtEpoch: Long = 1700000000L
    private var endAtEpoch: Long = 1800000000L
    private var buyX: Int? = null
    private var payY: Int? = null
    private var percent: Int? = null

    fun withId(id: String) = apply { this.id = id }
    fun withType(type: String) = apply { this.type = type }
    fun withProductIds(productsIds: List<String>) = apply {
        this.productIds = productsIds.toListString()
    }
    fun withValue(value: Double) = apply { this.value = value }
    fun withBuyQuantity(buyQuantity: Int?) = apply { this.buyQuantity = buyQuantity }
    fun withStartTime(startAtEpoch: Long) = apply { this.startAtEpoch = startAtEpoch }
    fun withEndTime(endAtEpoch: Long) = apply { this.endAtEpoch = endAtEpoch }
    fun withBuyX(buyX: Int?) = apply { this.buyX = buyX }
    fun withPayY(payY: Int?) = apply { this.payY = payY }
    fun withPercent(percent: Int?) = apply { this.percent = percent }

    fun build() = PromotionEntity(
        id = id,
        type = type,
        productIds = productIds,
        startAtEpoch = startAtEpoch,
        endAtEpoch = endAtEpoch,
        buyX = buyX,
        payY = payY,
        percent = percent,
    )
}

fun promotionEntity(block: PromotionEntityBuilder.() -> Unit = {}) = PromotionEntityBuilder().apply(block).build()
