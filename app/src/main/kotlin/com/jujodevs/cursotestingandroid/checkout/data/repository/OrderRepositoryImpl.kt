package com.jujodevs.cursotestingandroid.checkout.data.repository

import com.jujodevs.cursotestingandroid.checkout.data.mapper.toDomain
import com.jujodevs.cursotestingandroid.checkout.domain.model.OrderConfirmation
import com.jujodevs.cursotestingandroid.checkout.domain.repository.OrderRepository
import com.jujodevs.cursotestingandroid.productlist.data.remote.RemoteDataSource
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource
): OrderRepository {
    override suspend fun placeOrder(): Result<OrderConfirmation> {
        return remoteDataSource.placeOrder().map { it.toDomain() }
    }
}
