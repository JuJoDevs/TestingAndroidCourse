package com.jujodevs.cursotestingandroid.productlist.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jujodevs.cursotestingandroid.R
import com.jujodevs.cursotestingandroid.cart.presentation.CartUiState
import com.jujodevs.cursotestingandroid.cart.presentation.CartViewModel
import com.jujodevs.cursotestingandroid.core.presentation.ui.ObserveAsEvents
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.PRODUCT_LIST_LIST
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.PRODUCT_LIST_LOADING
import com.jujodevs.cursotestingandroid.productlist.domain.model.Product
import com.jujodevs.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import com.jujodevs.cursotestingandroid.productlist.domain.model.SortOption
import com.jujodevs.cursotestingandroid.productlist.presentation.components.FiltersMenu
import com.jujodevs.cursotestingandroid.productlist.presentation.components.HomeTopAppBar
import com.jujodevs.cursotestingandroid.productlist.presentation.components.ProductItem
import com.jujodevs.cursotestingandroid.ui.theme.CursoTestingAndroidTheme

@Composable
fun ProductListScreen(
    productListViewModel: ProductListViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel(),
    navigateToSettings: () -> Unit,
    navigateToProductDetail: (String) -> Unit,
    navigateToCart: () -> Unit,
) {
    val uiState by productListViewModel.uiState.collectAsStateWithLifecycle()
    val cartUiState by cartViewModel.uiState.collectAsStateWithLifecycle()
    val filterVisible by productListViewModel.filterVisible.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(productListViewModel.events) { event ->
        when (event) {
            is ProductListEvent.ShowMessage -> {
                snackbarHostState.showSnackbar(event.message)
            }

            ProductListEvent.NavigateToSettings -> navigateToSettings()
            is ProductListEvent.NavigateToProductDetail -> navigateToProductDetail(event.productId)
            ProductListEvent.NavigateToCart -> navigateToCart()
        }
    }

    val cartItemCount = remember(cartUiState) {
        when (val state = cartUiState) {
            is CartUiState.Error -> 0
            CartUiState.Loading -> 0
            is CartUiState.Success -> {
                state.cartItems.sumOf { it.cartItem.quantity }
            }
        }
    }

    ProductListContent(
        uiState = uiState,
        cartItemCount = cartItemCount,
        filterVisible = filterVisible,
        snackbarHostState = snackbarHostState,
        onAction = productListViewModel::onAction
    )
}

@Composable
internal fun ProductListContent(
    uiState: ProductListUiState,
    cartItemCount: Int,
    filterVisible: Boolean,
    snackbarHostState: SnackbarHostState,
    onAction: (ProductListAction) -> Unit,
) {
    Scaffold(
        topBar = {
            HomeTopAppBar(
                filterVisible = filterVisible,
                cartItemCount = cartItemCount,
                onAction = onAction
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                ProductListUiState.Loading -> {
                    CircularProgressIndicator(Modifier.testTag(PRODUCT_LIST_LOADING))
                }

                is ProductListUiState.Error -> {
                    Text(
                        text = stringResource(R.string.product_list_error),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                is ProductListUiState.Success -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        AnimatedVisibility(filterVisible) {
                            FiltersMenu(
                                state = state,
                                onAction = onAction
                            )
                        }

                        Text(
                            text = stringResource(R.string.product_list_count, state.products.size),
                            modifier = Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                        if (state.products.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(
                                        text = stringResource(R.string.product_list_empty_icon),
                                        style = MaterialTheme.typography.displayMedium
                                    )
                                    Text(
                                        text = stringResource(R.string.product_list_no_products),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.testTag(PRODUCT_LIST_LIST)
                            ) {
                                items(state.products) { item ->
                                    ProductItem(
                                        item = item,
                                        onClick = {
                                            onAction(
                                                ProductListAction.NavToProductDetail(item)
                                            )
                                        },
                                        modifier = Modifier.animateItem()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun SettingsScreenPreview() {
    val products =
        listOf(
            ProductWithPromotion(
                product = Product(
                    id = "causae",
                    name = "Maryanne Bird",
                    description = "principes",
                    price = 2.3,
                    category = "ferri",
                    stock = 5867,
                    imageUrl = "https://www.google.com/#q=dicunt"
                )
            )
        )

    CursoTestingAndroidTheme {
        ProductListContent(
            uiState = ProductListUiState.Success(
                products = products,
                categories = listOf(
                    "category-1",
                    "category-2",
                    "category-3"
                ),
                selectedCategory = "category-2",
                sortOption = SortOption.NONE,
            ),
            cartItemCount = 1,
            filterVisible = true,
            snackbarHostState = remember { SnackbarHostState() },
            onAction = { }
        )
    }
}
