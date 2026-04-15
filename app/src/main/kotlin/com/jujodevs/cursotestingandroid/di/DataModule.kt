package com.jujodevs.cursotestingandroid.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.jujodevs.cursotestingandroid.cart.data.local.database.dao.CartDao
import com.jujodevs.cursotestingandroid.cart.data.repository.CartRepositoryImpl
import com.jujodevs.cursotestingandroid.cart.domain.repository.CartRepository
import com.jujodevs.cursotestingandroid.core.data.coroutines.DefaultDispatchersProvider
import com.jujodevs.cursotestingandroid.core.domain.coroutines.DispatchersProvider
import com.jujodevs.cursotestingandroid.core.data.local.database.MiniMarketDatabase
import com.jujodevs.cursotestingandroid.core.data.time.SystemClock
import com.jujodevs.cursotestingandroid.core.domain.time.Clock
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
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
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
    fun providesDatabase(@ApplicationContext context: Context):MiniMarketDatabase{
        return Room.databaseBuilder(
            context = context,
            klass = MiniMarketDatabase::class.java,
            name = "minimarket_database"
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
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun providesSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository = settingsRepositoryImpl

    @Provides
    @Singleton
    fun providesClock(systemClock: SystemClock): Clock = systemClock
}