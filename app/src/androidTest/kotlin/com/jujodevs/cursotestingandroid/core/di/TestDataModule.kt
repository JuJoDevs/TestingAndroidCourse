package com.jujodevs.cursotestingandroid.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.jujodevs.cursotestingandroid.cart.data.local.database.dao.CartDao
import com.jujodevs.cursotestingandroid.cart.data.repository.CartRepositoryImpl
import com.jujodevs.cursotestingandroid.cart.domain.repository.CartRepository
import com.jujodevs.cursotestingandroid.core.data.coroutines.DefaultDispatchersProvider
import com.jujodevs.cursotestingandroid.core.data.local.database.MiniMarketDatabase
import com.jujodevs.cursotestingandroid.core.data.time.SystemClock
import com.jujodevs.cursotestingandroid.core.domain.coroutines.DispatchersProvider
import com.jujodevs.cursotestingandroid.core.domain.time.Clock
import com.jujodevs.cursotestingandroid.di.DataModule
import com.jujodevs.cursotestingandroid.productlist.data.local.database.dao.ProductDao
import com.jujodevs.cursotestingandroid.productlist.data.local.database.dao.PromotionDao
import com.jujodevs.cursotestingandroid.productlist.data.repository.ProductRepositoryImpl
import com.jujodevs.cursotestingandroid.productlist.data.repository.PromotionRepositoryImpl
import com.jujodevs.cursotestingandroid.productlist.data.repository.SettingsRepositoryImpl
import com.jujodevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.jujodevs.cursotestingandroid.productlist.domain.repository.PromotionRepository
import com.jujodevs.cursotestingandroid.productlist.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

private val Context.testingDataStore: DataStore<Preferences> by preferencesDataStore(name = "testing_settings")

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DataModule::class],
)
object TestDataModule {
    @Provides
    @Singleton
    fun provideDispatchersProvider(
        defaultDispatchersProvider: DefaultDispatchersProvider
    ): DispatchersProvider = defaultDispatchersProvider

    @Provides
    fun providesProductDao(database: MiniMarketDatabase): ProductDao {
        return database.productDao()
    }

    @Provides
    fun providesPromotionDao(database: MiniMarketDatabase): PromotionDao {
        return database.promotionDao()
    }

    @Provides
    fun providesCartDao(database: MiniMarketDatabase): CartDao {
        return database.cartDao()
    }

    @Provides
    @Singleton
    fun providesDatabase():MiniMarketDatabase{
        val context = ApplicationProvider.getApplicationContext<Context>()
        return Room.inMemoryDatabaseBuilder(
            context,
            MiniMarketDatabase::class.java
        ).build()
    }

    @Provides
    @Singleton
    fun providesProductRepository(
        productRepositoryImpl: ProductRepositoryImpl
    ): ProductRepository = productRepositoryImpl

    @Provides
    @Singleton
    fun providesPromotionRepository(
        promotionRepositoryImpl: PromotionRepositoryImpl
    ): PromotionRepository = promotionRepositoryImpl

    @Provides
    @Singleton
    fun providesCartRepository(
        cartRepositoryImpl: CartRepositoryImpl
    ): CartRepository = cartRepositoryImpl

    @Provides
    @Singleton
    fun provideDataStore(): DataStore<Preferences> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return context.testingDataStore.apply {
            runBlocking { edit { preferences -> preferences.clear() } }
        }
    }

    @Provides
    fun providesSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository = settingsRepositoryImpl

    @Provides
    @Singleton
    fun providesClock(systemClock: SystemClock): Clock = systemClock
}