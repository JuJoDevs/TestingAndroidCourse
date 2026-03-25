package com.jujodevs.cursotestingandroid.detail.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.jujodevs.cursotestingandroid.core.presentation.components.MarketTopAppBar
import com.jujodevs.cursotestingandroid.core.presentation.ui.ObserveAsEvents
import com.jujodevs.cursotestingandroid.detail.presentation.components.AddToCartButton
import com.jujodevs.cursotestingandroid.productlist.domain.model.ProductPromotion

@Composable
fun ProductDetailScreen(
    productId: String,
    onBack: () -> Unit,
    productDetailViewModel: ProductDetailViewModel = hiltViewModel(),
) {

    val uiState by productDetailViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(productId) {
        productDetailViewModel.onAction(ProductDetailAction.LoadProduct(productId))
    }

    ObserveAsEvents(productDetailViewModel.events) { event ->
        when(event) {
            ProductDetailEvent.InsufficientStockError -> {
                snackbarHostState.showSnackbar("No hay suficiente stock")
            }
            ProductDetailEvent.NetworkError -> {
                snackbarHostState.showSnackbar("No hay internet compruebe su conexión")
            }
            ProductDetailEvent.UnknownError -> {
                snackbarHostState.showSnackbar("Error inesperado, vuelva a intentarlo")
            }

            ProductDetailEvent.SuccessAddToCart -> {
                snackbarHostState.showSnackbar("Producto añadido al carrito")
            }
        }
    }

    Scaffold(
        topBar = {
            MarketTopAppBar(
                title = uiState.item?.product?.name.orEmpty(),
                onBack = { onBack() }
            )
        },
        bottomBar = {
            BottomAppBar {
                AddToCartButton(
                    product = uiState.item?.product,
                    isLoading = uiState.isLoading,
                    addToCart = { productDetailViewModel.onAction(ProductDetailAction.AddToCart) }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                uiState.item?.let {
                    val product = it.product
                    val promotion = it.promotion
                    val discountedPrice = when (promotion) {
                        is ProductPromotion.Percent -> promotion.discountedPrice
                        is ProductPromotion.BuyXPayY -> null
                        null -> null
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                AsyncImage(
                                    model = product.imageUrl,
                                    contentDescription = product.name,
                                    contentScale = ContentScale.Crop,
                                    placeholder = rememberVectorPainter(Icons.Default.Image),
                                    error = rememberVectorPainter(Icons.Default.BrokenImage),
                                )

                                Text(
                                    text = product.name,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                )

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                ) {
                                    Text(
                                        text = product.category,
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 6.dp
                                        ),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    )
                                }

                                if (product.description.isNotBlank()) {
                                    Text(
                                        text = product.description,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }

                                HorizontalDivider()

                                if (discountedPrice != null) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = product.price.toString(),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textDecoration = TextDecoration.LineThrough,
                                        )
                                        Text(
                                            text = discountedPrice.toString(),
                                            style = MaterialTheme.typography.displaySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.errorContainer,
                                    ) {
                                        Text(
                                            text = "${(promotion as? ProductPromotion.Percent)?.percent?.toInt() ?: ""}% OFF",
                                            modifier = Modifier.padding(
                                                horizontal = 12.dp,
                                                vertical = 6.dp
                                            ),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                        )
                                    }
                                } else {
                                    Text(
                                        text = product.price.toString(),
                                        style = MaterialTheme.typography.displaySmall,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }

                                if (promotion is ProductPromotion.BuyXPayY) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.errorContainer,
                                    ) {
                                        Text(
                                            text = "PROMO: ${promotion.label}",
                                            modifier = Modifier.padding(
                                                horizontal = 12.dp,
                                                vertical = 6.dp
                                            ),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                        )
                                    }
                                }

                                HorizontalDivider()

                                val hasStock = product.stock > 0
                                val stockContainerColor =
                                    if (hasStock) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.errorContainer
                                val stockContentColor =
                                    if (hasStock) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onErrorContainer

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "Stock disponible",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = stockContainerColor,
                                    ) {
                                        Text(
                                            text =
                                                if (hasStock) "${product.stock} unidades"
                                                else "Sin stock",
                                            modifier =
                                                Modifier.padding(
                                                    horizontal = 12.dp,
                                                    vertical = 6.dp
                                                ),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = stockContentColor,
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
}
