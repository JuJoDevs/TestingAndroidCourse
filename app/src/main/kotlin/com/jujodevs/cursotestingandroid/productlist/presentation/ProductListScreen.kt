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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jujodevs.cursotestingandroid.core.presentation.ui.ObserveAsEvents
import com.jujodevs.cursotestingandroid.productlist.presentation.components.FiltersMenu
import com.jujodevs.cursotestingandroid.productlist.presentation.components.HomeTopAppBar
import com.jujodevs.cursotestingandroid.productlist.presentation.components.ProductItem

@Composable
fun ProductListScreen(
    productListViewModel: ProductListViewModel = hiltViewModel(),
) {
    val uiState by productListViewModel.uiState.collectAsStateWithLifecycle()
    val filtersVisible by productListViewModel.filtersVisible.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(productListViewModel.events) { event ->
        when (event) {
            is ProductListEvent.ShowMessage -> {
                snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = { HomeTopAppBar(
            filtersVisible = filtersVisible,
            onAction = productListViewModel::onAction
        ) },
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
                    CircularProgressIndicator()
                }

                is ProductListUiState.Error -> {
                    Text(
                        text = "ERROR",
                        fontSize = 30.sp,
                        color = Color.Red
                    )
                }

                is ProductListUiState.Success -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        AnimatedVisibility(filtersVisible) {
                            FiltersMenu(
                                state = state,
                                onAction = productListViewModel::onAction
                            )
                        }

                        Text(
                            text = "${state.products.size} productos",
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
                                        text = "🔍",
                                        style = MaterialTheme.typography.displayMedium
                                    )
                                    Text(
                                        text = "No se encontraron productos",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                        } else {
                            LazyColumn {
                                items(state.products) { product ->
                                    ProductItem(
                                        product = product,
                                        onClick = {})
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
