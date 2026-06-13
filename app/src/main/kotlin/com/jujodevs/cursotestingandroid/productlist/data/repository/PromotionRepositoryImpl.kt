package com.jujodevs.cursotestingandroid.productlist.data.repository

import com.jujodevs.cursotestingandroid.core.domain.coroutines.DispatchersProvider
import com.jujodevs.cursotestingandroid.productlist.data.local.LocalDataSource
import com.jujodevs.cursotestingandroid.productlist.data.mappers.toDomain
import com.jujodevs.cursotestingandroid.productlist.data.mappers.toEntity
import com.jujodevs.cursotestingandroid.productlist.data.remote.RemoteDataSource
import com.jujodevs.cursotestingandroid.productlist.domain.model.Promotion
import com.jujodevs.cursotestingandroid.productlist.domain.repository.PromotionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

class PromotionRepositoryImpl
    @Inject
    constructor(
        private val remoteDataSource: RemoteDataSource,
        private val localDataSource: LocalDataSource,
        private val dispatchers: DispatchersProvider,
        private val json: Json,
    ) : PromotionRepository {
        private val refreshScope = CoroutineScope(SupervisorJob() + dispatchers.io)

        @OptIn(ExperimentalAtomicApi::class)
        private val isRefreshing = AtomicBoolean(false)

        @OptIn(ExperimentalAtomicApi::class)
        override fun getActivePromotions(): Flow<List<Promotion>> {
            return localDataSource
                .getAllPromotions()
                .map { entities -> entities.mapNotNull { entity -> entity.toDomain(json) } }
                .onStart {
                    if (!isRefreshing.compareAndSet(
                            expectedValue = false,
                            newValue = true,
                        )
                    ) {
                        return@onStart
                    }

                    refreshScope.launch {
                        try {
                            refreshPromotions()
                        } catch (e: Exception) {
                            coroutineContext.ensureActive()
                            // refresh log
                        } finally {
                            isRefreshing.store(false)
                        }
                    }
                }.catch {
                    // Important log
                }
        }

        override suspend fun refreshPromotions() {
            withContext(dispatchers.io) {
                val promotions = remoteDataSource.getPromotions().getOrThrow()
                val promotionsEntity = promotions.mapNotNull { it.toEntity(json) }
                localDataSource.savePromotions(promotionsEntity)
            }
        }
    }
