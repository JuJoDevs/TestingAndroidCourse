package com.jujodevs.cursotestingandroid.productlist.data.local

import com.jujodevs.cursotestingandroid.productlist.data.local.database.dao.ProductDao
import com.jujodevs.cursotestingandroid.productlist.data.local.database.dao.PromotionDao
import com.jujodevs.cursotestingandroid.productlist.data.local.database.entity.ProductEntity
import com.jujodevs.cursotestingandroid.productlist.data.local.database.entity.PromotionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalDataSource @Inject constructor(
    private val productDao: ProductDao,
    private val promotionDao: PromotionDao,
) {
    fun getAllProducts(): Flow<List<ProductEntity>> = productDao.getAllProducts()

    fun getProductById(id: String): Flow<ProductEntity?> = productDao.getProductById(id)

    suspend fun saveProducts(products: List<ProductEntity>) {
        productDao.replaceAll(products)
    }

    fun getAllPromotions(): Flow<List<PromotionEntity>> = promotionDao.getAllPromotions()

    suspend fun savePromotions(promotions: List<PromotionEntity>) {
        promotionDao.replaceAll(promotions)
    }
}