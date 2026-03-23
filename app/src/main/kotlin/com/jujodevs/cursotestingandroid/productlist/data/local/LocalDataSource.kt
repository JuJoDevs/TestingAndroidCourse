package com.jujodevs.cursotestingandroid.productlist.data.local

import com.jujodevs.cursotestingandroid.cart.data.local.database.dao.CartItemDao
import com.jujodevs.cursotestingandroid.cart.data.local.database.entity.CartItemEntity
import com.jujodevs.cursotestingandroid.productlist.data.local.database.dao.ProductDao
import com.jujodevs.cursotestingandroid.productlist.data.local.database.dao.PromotionDao
import com.jujodevs.cursotestingandroid.productlist.data.local.database.entity.ProductEntity
import com.jujodevs.cursotestingandroid.productlist.data.local.database.entity.PromotionEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalDataSource @Inject constructor(
    private val productDao: ProductDao,
    private val promotionDao: PromotionDao,
    private val cartItemDao: CartItemDao,
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

    fun getAllCartItems(): Flow<List<CartItemEntity>> = cartItemDao.getAllCartItems()

    suspend fun getCartItemById(productId: String): CartItemEntity? =
        cartItemDao.getCartItemById(productId)

    suspend fun insertCartItem(cartItemEntity: CartItemEntity): Result<Unit> =
        try {
            cartItemDao.insertCartItem(cartItemEntity)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun updateCartItem(cartItemEntity: CartItemEntity): Result<Unit> =
        try {
            cartItemDao.updateCartItem(cartItemEntity)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun deleteCartItem(cartItemEntity: CartItemEntity): Result<Unit> =
        try {
            cartItemDao.deleteCartItem(cartItemEntity)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun clearCart(cartItemEntity: CartItemEntity): Result<Unit> =
        try {
            cartItemDao.clearCart()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
}