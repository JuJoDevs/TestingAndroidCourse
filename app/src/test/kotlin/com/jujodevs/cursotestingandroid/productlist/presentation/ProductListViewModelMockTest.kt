package com.jujodevs.cursotestingandroid.productlist.presentation

import com.jujodevs.cursotestingandroid.core.MainDispatcherRule
import com.jujodevs.cursotestingandroid.core.runTurbineTest
import com.jujodevs.cursotestingandroid.productlist.domain.model.SortOption
import com.jujodevs.cursotestingandroid.productlist.domain.repository.SettingsRepository
import com.jujodevs.cursotestingandroid.productlist.domain.usecase.GetProductsUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ProductListViewModelMockTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val settingsRepository: SettingsRepository =
        mockk(relaxed = true) {
            every { selectedCategory } returns flowOf(null)
            every { sortOption } returns flowOf(SortOption.NONE)
            every { inStockOnly } returns flowOf(false)
            every { filtersVisible } returns flowOf(false)
        }
    private val getProductsUseCase: GetProductsUseCase = mockk()

    lateinit var viewModel: ProductListViewModel

    @Before
    fun setUp() {
        every { getProductsUseCase.invoke() } returns flowOf()
        viewModel =
            ProductListViewModel(
                getProductsUseCase = getProductsUseCase,
                settingsRepository = settingsRepository,
            )
    }

    @Test
    fun `GIVEN category WHEN set category THEN delegates to settings repository`() =
        runTurbineTest {
            val category = "pasta"

            viewModel.onAction(ProductListAction.SetCategory(category))

            coVerify(exactly = 1) { settingsRepository.setSelectedCategory(category) }
        }

    @Test
    fun `GIVEN sort option WHEN set sort option THEN delegates to settings repository`() =
        runTurbineTest {
            val option = SortOption.DISCOUNT

            viewModel.onAction(ProductListAction.SetOrderSelected(option))

            coVerify(exactly = 1) { settingsRepository.setSortOption(option) }
        }
}
