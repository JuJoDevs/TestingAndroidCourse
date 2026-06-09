package com.jujodevs.cursotestingandroid.core.mothers

import com.jujodevs.cursotestingandroid.core.builders.promotion
import com.jujodevs.cursotestingandroid.productlist.domain.model.ProductPromotion

object PromotionMother {
    fun percent() = ProductPromotion.Percent(
        percent = 25.0,
        discountedPrice = 4.65,
        label = "25%"
    )
    fun buyXGetY() = ProductPromotion.BuyXPayY(2, 1, "2x1")
}