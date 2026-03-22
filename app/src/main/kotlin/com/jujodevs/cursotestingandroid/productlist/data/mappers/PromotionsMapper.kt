package com.jujodevs.cursotestingandroid.productlist.data.mappers

import com.jujodevs.cursotestingandroid.core.domain.safeRunCatching
import com.jujodevs.cursotestingandroid.productlist.data.local.database.entity.PromotionEntity
import com.jujodevs.cursotestingandroid.productlist.data.remote.response.PromotionResponse
import com.jujodevs.cursotestingandroid.productlist.domain.model.Promotion
import com.jujodevs.cursotestingandroid.productlist.domain.model.PromotionType
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.time.Instant

fun PromotionEntity.toDomain(json: Json): Promotion? {
    val decodedProductsIds = productIds.toDecodedProductsIds(json)
    val finalType = type.toPromotionType()

    return if (decodedProductsIds == null || finalType == null) null
    else {
        val finalPrice = when(finalType) {
            PromotionType.PERCENT -> percent
            PromotionType.BUY_X_PAY_Y -> payY
        }?.toDouble()

        if (finalPrice == null) null
        else Promotion(
            id = id,
            productsIds = decodedProductsIds,
            type = finalType,
            value = finalPrice,
            buyQuantity = buyX,
            startTime = Instant.ofEpochSecond(startAtEpoch),
            endTime = Instant.ofEpochSecond(endAtEpoch),
        )
    }
}

private fun String.toDecodedProductsIds(json: Json): List<String>? {
    return safeRunCatching { json.decodeFromString(
        deserializer = ListSerializer(elementSerializer = String.serializer()),
        string = this,
    ) }.getOrNull()
}

private fun String.toPromotionType(): PromotionType? {
    return safeRunCatching {
        PromotionType.valueOf(this.trim().uppercase())
    }.getOrNull()
}

fun PromotionResponse.toEntity(json: Json): PromotionEntity? {
    return if (isNotValid()) null
    else {
        val productsIds = listOf(productId!!)
        val productIdsJson = json.encodeToString(
            serializer = ListSerializer(String.serializer()),
            value = productsIds,
        )

        PromotionEntity(
            id = id!!,
            productIds = productIdsJson,
            type = type!!,
            percent = percent,
            buyX = buyX,
            payY = payY,
            startAtEpoch = startAtEpoch!!,
            endAtEpoch = endAtEpoch!!,
        )
    }
}

private fun PromotionResponse.isNotValid() =
    id == null || productId == null || type == null|| startAtEpoch == null || endAtEpoch == null
