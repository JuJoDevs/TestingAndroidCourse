package com.jujodevs.cursotestingandroid.productlist.data.mappers

import com.jujodevs.cursotestingandroid.productlist.data.local.database.entity.ProductEntity
import com.jujodevs.cursotestingandroid.productlist.data.remote.response.ProductResponse
import com.jujodevs.cursotestingandroid.productlist.domain.model.Product

fun ProductResponse.toEntity(): ProductEntity? {
    val finalPrice = priceCents?.div(100.0) ?: 0.0

    return if (id == null || name == null) null
    else ProductEntity(
        id = id,
        name = name,
        description = description,
        price = finalPrice,
        category = category,
        stock = stock,
        imageUrl = imageUrl
    )
}

fun ProductEntity.toDomain(): Product? {
    return if (category == null) null
    else Product(
        id = id,
        name = name,
        description = description.orEmpty(),
        price = price,
        category = category,
        stock = stock ?: 0,
        imageUrl = imageUrl
    )
}
