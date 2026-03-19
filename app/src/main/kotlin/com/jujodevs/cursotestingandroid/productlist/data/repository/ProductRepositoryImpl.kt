package com.jujodevs.cursotestingandroid.productlist.data.repository

import com.jujodevs.cursotestingandroid.core.domain.coroutines.DispatchersProvider
import com.jujodevs.cursotestingandroid.productlist.data.local.LocalDataSource
import com.jujodevs.cursotestingandroid.productlist.data.mappers.toDomain
import com.jujodevs.cursotestingandroid.productlist.data.mappers.toEntity
import com.jujodevs.cursotestingandroid.productlist.data.remote.RemoteDataSource
import com.jujodevs.cursotestingandroid.productlist.domain.model.Product
import com.jujodevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
    private val dispatchers: DispatchersProvider,
): ProductRepository {

    private val refreshScope = CoroutineScope(SupervisorJob() + dispatchers.io)
    private val refreshMutex = Mutex()

    override fun getProducts(): Flow<List<Product>> {
        return localDataSource.getAllProducts()
            .map { entities -> entities.mapNotNull { entity -> entity.toDomain() } }
            .onStart {
                refreshScope.launch {
                    if (!refreshMutex.tryLock()) return@launch
                    try {
                        refreshProducts()
                    } catch (e: Exception) {
                        coroutineContext.ensureActive()
                        // refresh log
                    } finally {
                        refreshMutex.unlock()
                    }
                }
            }.catch {
                // Important log
            }
    }

    override fun getProductById(id: String): Flow<Product?> {
        TODO("Not yet implemented")
    }

    override suspend fun refreshProducts() {
        withContext(dispatchers.io) {
            val products = remoteDataSource.getProducts().getOrThrow()
            val productsEntity = products.mapNotNull { it.toEntity() }
            localDataSource.saveProducts(productsEntity)
        }
    }
}