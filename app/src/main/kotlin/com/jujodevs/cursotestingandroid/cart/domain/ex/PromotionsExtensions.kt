package com.jujodevs.cursotestingandroid.cart.domain.ex

import com.jujodevs.cursotestingandroid.productlist.domain.model.Promotion
import java.time.Instant

fun List<Promotion>.activeAt(now: Instant): List<Promotion> =
    this.filter { promotion ->
        promotion.startTime <= now && promotion.endTime >= now
    }
