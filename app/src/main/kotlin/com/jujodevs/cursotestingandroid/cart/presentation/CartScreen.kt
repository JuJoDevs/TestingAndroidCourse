package com.jujodevs.cursotestingandroid.cart.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.jujodevs.cursotestingandroid.cart.presentation.model.CartItemWithPromotion
import com.jujodevs.cursotestingandroid.core.presentation.components.MarketTopAppBar
import com.jujodevs.cursotestingandroid.core.presentation.components.QuantitySelector
import com.jujodevs.cursotestingandroid.core.presentation.ui.ObserveAsEvents
import java.text.NumberFormat
import java.util.Currency

@Composable
fun CartScreen(
    onBack: () -> Unit,
    cartViewModel: CartViewModel = hiltViewModel(),
) {
    val uiState by cartViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(cartViewModel.events) { event ->
        when (event) {
            is CartEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            MarketTopAppBar(
                title = "Carrito",
                onBack = onBack,
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            CartUiState.Loading -> {
                CartLoadingStateScreen(
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            is CartUiState.Error -> {
                CartErrorStateScreen(
                    state = state,
                    onRetrySelected = { cartViewModel.onAction(CartAction.LoadCart) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            is CartUiState.Success -> CartSuccessStateScreen(
                state = state,
                onAction = { cartViewModel.onAction(it) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}

@Composable
private fun CartLoadingStateScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun CartErrorStateScreen(
    state: CartUiState.Error,
    onRetrySelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Error: ${state.message}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onRetrySelected
        ) {
            Text(text = "Reintentar")
        }
    }
}

@Composable
fun CartSuccessStateScreen(
    state: CartUiState.Success,
    onAction: (CartAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.padding(16.dp)) {
        if (state.cartItems.isEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                Text(
                    text = "🛒",
                    style = MaterialTheme.typography.displayLarge,
                )
                Text(
                    "Tu carrito está vacío",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Agrega productos para comenzar",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.cartItems) { itemWithProduct ->
                    CartItemCard(
                        itemWithProduct = itemWithProduct,
                        onAction = onAction,
                    )
                }
            }
        }
    }
}

@Composable
fun CartItemCard(
    itemWithProduct: CartItemWithPromotion,
    onAction: (CartAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val product = itemWithProduct.productWithPromotion.product
    val promotion = itemWithProduct.productWithPromotion.promotion
    val cartItem = itemWithProduct.cartItem

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance()
            .apply {
                currency = Currency.getInstance("USD")
            }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.weight(1f),
            )
            Column(
                modifier = Modifier.weight(3f),
            ) {
                Text(
                    text = product.name
                )
                // Promo
                Text(
                    text = "Total: ${currencyFormatter.format(product.price)}"
                )
                QuantitySelector(
                    quantity = cartItem.quantity.toString(),
                    canDecrease = cartItem.quantity > 1,
                    canIncrease = cartItem.quantity < product.stock,
                    onDecreaseClick = {
                        onAction(
                            CartAction.DecreaseQuantity(
                                cartItem.productId,
                                cartItem.quantity
                            )
                        )
                    },
                    onIncreaseClick = {
                        onAction(
                            CartAction.IncreaseQuantity(
                                cartItem.productId,
                                cartItem.quantity
                            )
                        )
                    },
                    modifier = Modifier.background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ),
                )
            }
        }
    }
}
