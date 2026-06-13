package com.jujodevs.cursotestingandroid.cart.data.mapper

import com.jujodevs.cursotestingandroid.cart.data.local.database.entity.CartItemEntity
import com.jujodevs.cursotestingandroid.cart.domain.model.CartItem

fun CartItemEntity.toDomain(): CartItem =
    CartItem(
        productId = this.productId,
        quantity = this.quantity,
    )

fun CartItem.toEntity(): CartItemEntity =
    CartItemEntity(
        productId = this.productId,
        quantity = this.quantity,
    )
