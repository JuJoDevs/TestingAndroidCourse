package com.jujodevs.cursotestingandroid.productlist.data.repository

import com.jujodevs.cursotestingandroid.core.domain.coroutines.DispatchersProvider
import com.jujodevs.cursotestingandroid.productlist.data.remote.RemoteDataSource
import com.jujodevs.cursotestingandroid.productlist.domain.model.Product
import com.jujodevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val dispatchers: DispatchersProvider,
): ProductRepository {
    override fun getProducts(): Flow<List<Product>> {
        TODO("Not yet implemented")
    }

    override fun getProductById(id: String): Flow<Product?> {
        TODO("Not yet implemented")
    }

    override suspend fun refreshProducts() {
        withContext(dispatchers.io) {
            remoteDataSource.getProducts()
        }
    }
}