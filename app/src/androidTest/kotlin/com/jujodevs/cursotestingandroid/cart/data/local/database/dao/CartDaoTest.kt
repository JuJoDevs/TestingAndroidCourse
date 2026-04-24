package com.jujodevs.cursotestingandroid.cart.data.local.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jujodevs.cursotestingandroid.core.builders.cartItemEntity
import com.jujodevs.cursotestingandroid.core.data.local.database.MiniMarketDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CartDaoTest {

    private lateinit var database: MiniMarketDatabase
    private lateinit var dao: CartDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MiniMarketDatabase::class.java
        ).build()
        dao = database.cartDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun givenEmptyCart_whenGetAllCartItems_thenEmitsEmptyList() = runTest {
        val item = dao.getAllCartItems().first()

        assertTrue(item.isEmpty())
    }

    @Test
    fun givenEmptyCart_whenInsertItem_thenItemIsPersisted() = runTest {
        val id = "id"
        val quantity = 3
        val cartItem = cartItemEntity { withProductId(id).withQuantity(quantity) }

        dao.insertCartItem(cartItem)

        val result = dao.getAllCartItems().first()
        assertTrue(result.size == 1)
        assertTrue(result.first().productId == id)
        assertTrue(result.first().quantity == quantity)
    }

    @Test
    fun givenInsertItem_whenGetItemById_thenReturnsCorrectItem() = runTest {
        val id = "id"
        val quantity = 3
        dao.insertCartItem(cartItemEntity { withProductId(id).withQuantity(quantity) })

        val result = dao.getCartItemById(id)

        assertTrue(result != null)
        assertTrue(result?.productId == id)
        assertTrue(result?.quantity == quantity)
    }

    @Test
    fun givenEmptyCart_whenGetItemById_thenReturnsNull() = runTest {
        val result = dao.getCartItemById("id")

        assertTrue(result == null)
    }

    @Test
    fun givenExistingItem_whenUpdateItemQuantity_thenQuantityIsUpdate() = runTest {
        val id = "id"
        val oldQuantity = 1
        val newQuantity = 67
        dao.insertCartItem(cartItemEntity { withProductId(id).withQuantity(oldQuantity) })

        dao.updateCartItem(cartItemEntity { withProductId(id).withQuantity(newQuantity) })

        val result = dao.getCartItemById(id)
        assertTrue(result?.quantity == newQuantity)
    }

    @Test
    fun givenItemCart_whenDeleteItem_thenCartBecomesEmpty() = runTest {
        val p = cartItemEntity { withProductId("id").withQuantity(1) }
        dao.insertCartItem(p)

        dao.deleteCartItem(p)

        val result = dao.getAllCartItems().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun givenMultipleItems_whenClearCart_thenAllItemsAreRemoved() = runTest {
        val p1 = cartItemEntity { withProductId("id1").withQuantity(1) }
        val p2 = cartItemEntity { withProductId("id2").withQuantity(1) }
        val p3 = cartItemEntity { withProductId("id3").withQuantity(1) }
        dao.insertCartItem(p1)
        dao.insertCartItem(p2)
        dao.insertCartItem(p3)

        dao.clearCart()

        val result = dao.getAllCartItems().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun givenExistingItemId_whenInsetDuplicateId_thenItemIsReplaced() = runTest {
        val id = "id"
        val oldQuantity = 1
        val newQuantity = 27
        val p1 = cartItemEntity { withProductId(id).withQuantity(oldQuantity) }
        val p2 = cartItemEntity { withProductId(id).withQuantity(newQuantity) }
        dao.insertCartItem(p1)

        dao.insertCartItem(p2)

        val result = dao.getCartItemById(id)
        assertTrue(result?.quantity == newQuantity)
    }
}