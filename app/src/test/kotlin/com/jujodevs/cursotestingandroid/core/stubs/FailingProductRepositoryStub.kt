package com.jujodevs.cursotestingandroid.core.stubs

import com.jujodevs.cursotestingandroid.productlist.domain.model.Product
import com.jujodevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class FailingProductRepositoryStub(
    private val exception: Throwable
): ProductRepository {
    override fun getProducts(): Flow<List<Product>> = flow {
        throw exception
    }

    override fun getProductById(id: String): Flow<Product?> = flow {
        throw exception
    }

    override fun getProductsById(ids: Set<String>): Flow<List<Product>> = flowOf()
    override suspend fun refreshProducts() {}
}