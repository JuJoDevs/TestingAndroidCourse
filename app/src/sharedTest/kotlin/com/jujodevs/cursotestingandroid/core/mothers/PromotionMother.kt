package com.jujodevs.cursotestingandroid.core.mothers

import com.jujodevs.cursotestingandroid.productlist.domain.model.ProductPromotion

object PromotionMother {
    val percent = ProductPromotion.Percent(
        percent = 25.0,
        discountedPrice = 4.65,
        label = "25%"
    )
    val buyXGetY = ProductPromotion.BuyXPayY(2, 1, "2x1")
}