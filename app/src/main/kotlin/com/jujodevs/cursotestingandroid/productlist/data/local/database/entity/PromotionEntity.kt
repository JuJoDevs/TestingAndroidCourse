package com.jujodevs.cursotestingandroid.productlist.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "promotions")
data class PromotionEntity(
    @PrimaryKey
    val id: String,
    val productIds: String,
    val type: String,
    val startAtEpoch: Long,
    val endAtEpoch: Long,
    val percent: Int? = null,
    val buyX: Int? = null,
    val payY: Int? = null,
)
