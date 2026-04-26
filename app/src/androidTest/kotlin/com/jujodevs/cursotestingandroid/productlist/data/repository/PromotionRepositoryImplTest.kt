package com.jujodevs.cursotestingandroid.productlist.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.jujodevs.cursotestingandroid.core.mockwebserver.MockWebServerUrlHolder
import com.jujodevs.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import com.jujodevs.cursotestingandroid.productlist.domain.repository.PromotionRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class PromotionRepositoryImplTest {
    @get:Rule(order = 0) val mockWebServer = MockWebServerRule()
    @get:Rule(order = 1) val hiltRule = HiltAndroidRule(this)

    @Inject lateinit var promotionRepository: PromotionRepository

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @After
    fun tearDown() {
        MockWebServerUrlHolder.baseUrl = ""
    }

    private fun readJson(fileName: String): String {
        val context = InstrumentationRegistry.getInstrumentation().context
        return context.assets.open(fileName).bufferedReader().use { it.readText() }
    }

    @Test
    fun givenActivePromotionsJson_whenRefreshIsCalled_thenFlowEmitsActivePromotions() = runTest {
        val json = readJson("promotions_percent.json")
        mockWebServer.server.enqueue(MockResponse().setBody(json).setResponseCode(200))

        promotionRepository.refreshPromotions()

        val promotions = promotionRepository.getActivePromotions().first()
        assertTrue(promotions.isNotEmpty())
        assertTrue(promotions.size == 1)
        assertTrue(promotions.find { it.id == "promo1" }?.productsIds == listOf("p1"))
    }

    @Test
    fun givenEmptyPromotionsJson_whenRefreshIsCAlled_thenListIsEmpty() = runTest {
        mockWebServer.server.enqueue(MockResponse().setBody("""{"promotions":[]}""").setResponseCode(200))

        promotionRepository.refreshPromotions()

        val promotions = promotionRepository.getActivePromotions().first()
        assertTrue(promotions.isEmpty())
    }

    @Test
    fun givenBuyXPayYJson_whenRefreshIsCalled_thenDomainMapsQuantitiesCorrectly() = runTest {
        val json = readJson("promotions_buy_x_pay_y.json")
        mockWebServer.server.enqueue(MockResponse().setBody(json).setResponseCode(200))

        promotionRepository.refreshPromotions()

        val promotions = promotionRepository.getActivePromotions().first()
        val promotion = promotions.find { it.id == "promo1" }
        assertTrue(promotions.isNotEmpty())
        assertTrue(promotions.size == 1)
        assertTrue(promotion?.productsIds == listOf("p1"))
        assertTrue(promotion?.value == 2.0)
        assertTrue(promotion?.buyQuantity == 3)
    }

    @Test(expected = Exception::class)
    fun givenServerReturns500_whenRefreshIsCalled_thenThrowException() = runTest {
        mockWebServer.server.enqueue(MockResponse().setResponseCode(500))

        promotionRepository.refreshPromotions()
    }

    @Test
    fun givenPromotionsEndpoint_whenRefreshIsCalled_thenRequestIsGetCorrectPath() = runTest {
        val json = readJson("promotions_buy_x_pay_y.json")
        mockWebServer.server.enqueue(MockResponse().setBody(json).setResponseCode(200))
        promotionRepository.refreshPromotions()

        val request = mockWebServer.server.takeRequest()

        assertEquals("GET", request.method)
        assertTrue(request.path?.contains("data/promotions.json") == true)
    }
}