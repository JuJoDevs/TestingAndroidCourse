package com.jujodevs.cursotestingandroid.cart.data.repository

import com.jujodevs.cursotestingandroid.cart.data.mapper.toDomain
import com.jujodevs.cursotestingandroid.cart.data.mapper.toEntity
import com.jujodevs.cursotestingandroid.cart.domain.model.CartItem
import com.jujodevs.cursotestingandroid.cart.domain.repository.CartRepository
import com.jujodevs.cursotestingandroid.core.domain.model.AppError
import com.jujodevs.cursotestingandroid.productlist.data.local.LocalDataSource
import com.jujodevs.cursotestingandroid.productlist.data.mappers.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CartRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
) : CartRepository {
    override fun getCartItems(): Flow<List<CartItem>> {
        return localDataSource.getAllCartItems()
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun addToCart(
        productId: String,
        quantity: Int,
    ) {
        val existingItem = localDataSource.getCartItemById(productId)
        if (existingItem != null) {
            val newQuantity = existingItem.quantity + quantity
            localDataSource.updateCartItem(existingItem.copy(quantity = newQuantity))
        } else {
            localDataSource.insertCartItem(CartItem(productId, quantity).toEntity())
        }
    }

    override suspend fun removeFromCart(productId: String) {
        val item = localDataSource.getCartItemById(productId) ?: throw AppError.NotFoundError
        localDataSource.deleteCartItem(item)
    }

    override suspend fun updateQuantity(
        productId: String,
        quantity: Int,
    ) {
        val item = localDataSource.getCartItemById(productId) ?: throw AppError.NotFoundError
        localDataSource.updateCartItem(item.copy(quantity = quantity))
    }

    override suspend fun clearCart() {
        localDataSource.clearCart()
    }

    override suspend fun getCartItemById(productId: String): CartItem? {
        return localDataSource.getCartItemById(productId)?.toDomain()
    }
}