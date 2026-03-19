package com.jujodevs.cursotestingandroid.productlist.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
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

@Composable
fun ProductListScreen(
    productListViewModel: ProductListViewModel = hiltViewModel()
) {
    val uiState by productListViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(productListViewModel.events) { event ->
        when(event) {
            is ProductListEvent.ShowMessage -> {
                snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
       snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when(val state = uiState) {
                ProductListUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is ProductListUiState.Error -> {
                    Text(text = "ERROR", fontSize = 30.sp, color = Color.Red)
                }
                is ProductListUiState.Success -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        FiltersMenu(
                            state = state,
                            onAction = { action ->
                                productListViewModel.onAction(action)
                            }
                        )
                        LazyColumn {
                            items(state.products) { product ->
                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                        .height(50.dp)
                                        .background(Color.Red),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(product.name)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}