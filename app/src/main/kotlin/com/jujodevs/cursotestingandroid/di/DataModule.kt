package com.jujodevs.cursotestingandroid.di

import com.jujodevs.cursotestingandroid.core.data.coroutines.DefaultDispatchersProvider
import com.jujodevs.cursotestingandroid.core.domain.coroutines.DispatchersProvider
import com.jujodevs.cursotestingandroid.productlist.data.repository.ProductRepositoryImpl
import com.jujodevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideDispatchersProvider(
        defaultDispatchersProvider: DefaultDispatchersProvider
    ): DispatchersProvider = defaultDispatchersProvider

    @Provides
    @Singleton
    fun providesProductRepository(
        productRepositoryImpl: ProductRepositoryImpl
    ): ProductRepository = productRepositoryImpl
}