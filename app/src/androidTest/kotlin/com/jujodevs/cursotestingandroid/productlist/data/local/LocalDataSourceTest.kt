package com.jujodevs.cursotestingandroid.productlist.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jujodevs.cursotestingandroid.core.builders.cartItemEntity
import com.jujodevs.cursotestingandroid.core.builders.productEntity
import com.jujodevs.cursotestingandroid.core.builders.promotionEntity
import com.jujodevs.cursotestingandroid.core.data.local.database.MiniMarketDatabase
import com.jujodevs.cursotestingandroid.core.toListString
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalDataSourceTest {

    private lateinit var database: MiniMarketDatabase
    private lateinit var localDataSource: LocalDataSource

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MiniMarketDatabase::class.java,
        ).build()
        localDataSource = LocalDataSource(
            database.productDao(),
            database.promotionDao(),
            database.cartDao(),
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun givenProducts_whenSaveAndGetAll_thenReturnsPersistedProducts() = runTest {
        val products = listOf(
            productEntity { withId("1") },
            productEntity { withId("2") },
        )

        localDataSource.saveProducts(products)

        val result = localDataSource.getAllProducts().first()
        assertEquals(products, result)
    }

    @Test
    fun givenSavedProduct_whenGetProductById_thenReturnsCorrectProduct() = runTest {
        val id = "1"
        val name = "milk"
        val products = listOf(
            productEntity { withId(id).withName(name) },
            productEntity { withId("2") },
        )

        localDataSource.saveProducts(products)

        val result = localDataSource.getProductById(id).first()
        assertEquals(name, result?.name)
    }

    @Test
    fun givenThreeProducts_whenGetProductsById_thenReturnsRequestedSubset() = runTest {
        val id1 = "1"
        val id2 = "2"
        val id3 = "3"
        val name1 = "milk"
        val name2 = "cookies"
        val name3 = "beef"
        val products = listOf(
            productEntity { withId(id1).withName(name1) },
            productEntity { withId(id2).withName(name2) },
            productEntity { withId(id3).withName(name3) },
        )
        localDataSource.saveProducts(products)

        val result = localDataSource.getProductsById(setOf(id1, id3)).first()

        assertEquals(2, result.size)
        assertTrue(result.any { it.name == name1 })
        assertTrue(result.any { it.name == name3 })
        assertTrue(result.none { it.name == name2 })
    }

    @Test
    fun givenPromotions_whenSaveAndGetAll_thenReturnsPersistedPromotions() = runTest {
        val id1 = "id1"
        val id2 = "id2"
        val pId1 = "p-id1"
        val productIds = listOf(pId1)
        val promotions = listOf(
            promotionEntity { withId(id1).withProductIds(productIds) },
            promotionEntity { withId(id2).withProductIds(productIds) },
        )

        localDataSource.savePromotions(promotions)

        val result = localDataSource.getAllPromotions().first()
        assertEquals(2, result.size)
        assertEquals(id1, promotions[0].id)
        assertEquals(id2, promotions[1].id)
        assertEquals(productIds.toListString(), promotions[0].productIds)
        assertEquals(productIds.toListString(), promotions[1].productIds)
    }

    @Test
    fun givenCartItem_whenInsertCartItem_thenReturnsSuccessAndItemSaved() = runTest {
        val id = "id1"
        val cartItem = cartItemEntity { withProductId(id).withQuantity(2) }

        val result = localDataSource.insertCartItem(cartItem)

        val items = localDataSource.getAllCartItems().first()
        assertTrue(result.isSuccess)
        assertTrue(items.size == 1)
        assertTrue(items.first().productId == id)
    }

    @Test
    fun givenExistingItem_whenUpdateCartItem_thenReturnsSuccessAndCartItemUpdated() = runTest {
        val id = "id1"
        val oldQuantity = 2
        val newQuantity = 67
        val oldCartItem = cartItemEntity { withProductId(id).withQuantity(oldQuantity) }
        val newCartItem = cartItemEntity { withProductId(id).withQuantity(newQuantity) }
        localDataSource.insertCartItem(oldCartItem)
        val result = localDataSource.insertCartItem(newCartItem)

        val item = localDataSource.getCartItemById(id)
        assertTrue(result.isSuccess)
        assertTrue(item?.quantity == newQuantity)
    }

    @Test
    fun givenCartItem_whenDeleteCartItem_thenReturnsSuccessAndCartIsEmpty() = runTest {
        val id = "id1"
        val cartItem = cartItemEntity { withProductId(id).withQuantity(2) }
        localDataSource.insertCartItem(cartItem)

        val result = localDataSource.deleteCartItem(cartItem)

        val items = localDataSource.getAllCartItems().first()
        assertTrue(result.isSuccess)
        assertTrue(items.isEmpty())
    }

    @Test
    fun givenMultipleCartItem_whenClearCart_thenReturnsSuccessAndCartIsEmpty() = runTest {
        val cartItem1 = cartItemEntity { withProductId("id1").withQuantity(2) }
        val cartItem2 = cartItemEntity { withProductId("id2").withQuantity(2) }
        val cartItem3 = cartItemEntity { withProductId("id3").withQuantity(2) }
        localDataSource.insertCartItem(cartItem1)
        localDataSource.insertCartItem(cartItem2)
        localDataSource.insertCartItem(cartItem3)

        val result = localDataSource.clearCart()

        val items = localDataSource.getAllCartItems().first()
        assertTrue(result.isSuccess)
        assertTrue(items.isEmpty())
    }
}