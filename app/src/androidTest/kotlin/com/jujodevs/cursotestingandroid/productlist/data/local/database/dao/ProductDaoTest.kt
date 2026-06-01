package com.jujodevs.cursotestingandroid.productlist.data.local.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jujodevs.cursotestingandroid.core.builders.productEntity
import com.jujodevs.cursotestingandroid.core.data.local.database.MiniMarketDatabase
import com.jujodevs.cursotestingandroid.core.runTurbineTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProductDaoTest {

    private lateinit var database: MiniMarketDatabase
    private lateinit var dao: ProductDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MiniMarketDatabase::class.java
        ).build()
        dao = database.productDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun givenEmptyDatabase_whenGetAllProducts_thenEmitsEmptyList() = runTest {
        val products = dao.getAllProducts().first()

        assertTrue(products.isEmpty())
    }

    @Test
    fun givenInsertedProduct_whenGetProductById_thenReturnsRow() = runTest {
        val id = "id"
        val p = productEntity { withId(id) }
        dao.insertAllProducts(listOf(p))

        val product = dao.getProductById(id).first()

        assertNotNull(product)
        assertEquals(p, product)
    }

    @Test
    fun givenThreeProducts_whenGetProductsByIds_thenReturnsRequestSubset() = runTest {
        val id1 = "id1"
        val id2 = "id2"
        val id3 = "id3"
        dao.insertAllProducts(listOf(
            productEntity { withId(id1) },
            productEntity { withId(id2) },
            productEntity { withId(id3) }
        ))

        val products = dao.getProductsById(listOf(id1, id3)).first()

        assertTrue(products.any { it.id == id1 })
        assertTrue(products.any { it.id == id3 })
        assertTrue(products.none { it.id == id2 })
    }

    @Test
    fun givenOldProducts_whenReplaceAll_thenReplacesOldProducts() = runTest {
        val oldId1 = "old-id1"
        val oldId2 = "old-id2"
        val id1 = "id1"
        val id2 = "id2"
        val id3 = "id3"
        dao.insertAllProducts(listOf(
            productEntity { withId(oldId1) },
            productEntity { withId(oldId2) },
        ))
        val newProducts = listOf(
            productEntity { withId(id1) },
            productEntity { withId(id2) },
            productEntity { withId(id3) },
        )

        dao.replaceAll(newProducts)

        val result = dao.getAllProducts().first()
        assertEquals(3, result.size)
        assertTrue(result.any { it.id == id1 || it.id == id2 || it.id == id3 })
        assertTrue(result.none { it.id == oldId1 || it.id == oldId2 })
    }

    @Test
    fun givenExistingProduct_whenInsertSameIdWithDifferentData_thenReplacesOldData() = runTest {
        val productId = "id"
        val name1 = "bread"
        val name2 = "milk"
        val p1 = productEntity { withId(productId).withName("bread") }
        val p2 = productEntity { withId(productId).withName("milk") }
        dao.insertAllProducts(listOf(p1))

        dao.insertAllProducts(listOf(p2))

        val result = dao.getAllProducts().first()
        assertTrue(result.size == 1)
        assertTrue(result.any { it.name == name2 })
        assertTrue(result.none { it.name == name1 })
    }

    @Test
    fun givenFlowSubscribed_whenInsertAfterSubscribe_thenEmitsUpdateList() = runTurbineTest {
        val id = "2"
        val products = dao.getAllProducts().testIn(this)
        assertTrue(products.awaitItem().isEmpty())

        dao.insertAllProducts(listOf(productEntity { withId(id) }))

        val updatedProducts = products.awaitItem()
        assertEquals(1, updatedProducts.size)
        assertEquals(id, updatedProducts.first().id)
        products.cancelAndIgnoreRemainingEvents()
    }
}