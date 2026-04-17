package com.jujodevs.cursotestingandroid.core.fakes

import com.jujodevs.cursotestingandroid.productlist.domain.model.Promotion
import com.jujodevs.cursotestingandroid.productlist.domain.repository.PromotionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakePromotionRepository: PromotionRepository {

    private var promotionsToRefresh: List<Promotion> = emptyList()
    private val _promotions = MutableStateFlow<List<Promotion>>(emptyList())

    fun setPromotionToRefresh(promotions: List<Promotion>) {
        promotionsToRefresh = promotions
    }

    fun setPromotions(promotions: List<Promotion>) {
        _promotions.update { promotions }
    }

    override fun getActivePromotions(): Flow<List<Promotion>> = _promotions.asStateFlow()

    override suspend fun refreshPromotions() {
        _promotions.update { promotionsToRefresh }
    }
}