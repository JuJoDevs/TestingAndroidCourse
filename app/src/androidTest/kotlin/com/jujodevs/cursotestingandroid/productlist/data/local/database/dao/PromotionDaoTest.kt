package com.jujodevs.cursotestingandroid.productlist.data.local.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.jujodevs.cursotestingandroid.core.builders.promotionEntity
import com.jujodevs.cursotestingandroid.core.data.local.database.MiniMarketDatabase
import com.jujodevs.cursotestingandroid.core.runTurbineTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PromotionDaoTest {

    private lateinit var database: MiniMarketDatabase
    private lateinit var dao: PromotionDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MiniMarketDatabase::class.java
        ).build()
        dao = database.promotionDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun givenEmptyDatabase_whenGetAllPromotions_thenEmitsEmptyList() = runTest {
        val promotions = dao.getAllPromotions().first()

        assertTrue(promotions.isEmpty())
    }

    @Test
    fun givenInsertedPromotions_whenGetAllPromotions_thenReturnsThem() = runTest {
        val p1 = promotionEntity { withId("id1") }
        val p2 = promotionEntity { withId("id2") }
        val list = listOf(p1, p2)
        dao.insertAllPromotions(list)

        val result = dao.getAllPromotions().first()

        assertEquals(2, result.size)
        assertTrue(result.containsAll(list))
    }

    @Test
    fun givenExistingPromotions_whenClearPromotions_thenDatabaseIsEmpty() = runTest {
        dao.insertAllPromotions(listOf(
            promotionEntity { withId("id1") },
            promotionEntity { withId("id2") }
        ))

        dao.clearPromotions()

        val result = dao.getAllPromotions().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun givenOldPromotions_whenReplaceAll_thenReplacesOldPromotions() = runTest {
        val oldId = "old-id"
        val newId = "new-id"
        dao.insertAllPromotions(listOf(promotionEntity { withId(oldId) }))
        val newPromotions = listOf(promotionEntity { withId(newId) })

        dao.replaceAll(newPromotions)

        val result = dao.getAllPromotions().first()
        assertEquals(1, result.size)
        assertEquals(newId, result.first().id)
        assertTrue(result.none { it.id == oldId })
    }

    @Test
    fun givenExistingPromotion_whenInsertSameIdWithDifferentData_thenReplacesOldData() = runTest {
        val promotionId = "id"
        val oldPercent = 10
        val newPercent = 20
        val p1 = promotionEntity { withId(promotionId).withPercent(oldPercent) }
        val p2 = promotionEntity { withId(promotionId).withPercent(newPercent) }
        dao.insertAllPromotions(listOf(p1))

        dao.insertAllPromotions(listOf(p2))

        val result = dao.getAllPromotions().first()
        assertEquals(1, result.size)
        assertEquals(newPercent, result.first().percent)
    }

    @Test
    fun givenFlowSubscribed_whenInsertAfterSubscribe_thenEmitsUpdatedList() = runTurbineTest {
        val id = "2"
        val promotionsFlow = dao.getAllPromotions().testIn(this)
        assertTrue(promotionsFlow.awaitItem().isEmpty())

        dao.insertAllPromotions(listOf(promotionEntity { withId(id) }))

        val updatedPromotions = promotionsFlow.awaitItem()
        assertEquals(1, updatedPromotions.size)
        assertEquals(id, updatedPromotions.first().id)
        promotionsFlow.cancelAndIgnoreRemainingEvents()
    }
}
