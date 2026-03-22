package com.jujodevs.cursotestingandroid.productlist.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.jujodevs.cursotestingandroid.productlist.domain.model.ProductPromotion
import com.jujodevs.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import java.util.Locale

@Composable
fun ProductItem(
    item: ProductWithPromotion,
    onClick: (ProductWithPromotion) -> Unit,
    modifier: Modifier = Modifier,
) {
    val product = item.product
    val promotion = item.promotion
    val promoBadge = when (promotion) {
        is ProductPromotion.BuyXPayY -> promotion.label
        is ProductPromotion.Percent -> promotion.label
        null -> null
    }
    var loadImageSuccess by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            )
            .clickable { onClick(item) },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    placeholder = rememberVectorPainter(Icons.Default.Image),
                    error = rememberVectorPainter(Icons.Default.BrokenImage),
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize(),
                    colorFilter =
                        if (loadImageSuccess) null
                        else ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                    onLoading = { loadImageSuccess = false },
                    onSuccess = { loadImageSuccess = true },
                    onError = { loadImageSuccess = false },
                )

                if (promoBadge != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .background(
                                color = MaterialTheme.colorScheme.error,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(
                                horizontal = 6.dp,
                                vertical = 2.dp
                            )
                    ) {
                        Text(
                            text = promoBadge,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onError,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                if (product.description.isNotBlank()) {
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (promotion is ProductPromotion.Percent) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Antes",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = String.format(
                                        Locale.getDefault(),
                                        "%.2f",
                                        product.price
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textDecoration = TextDecoration.LineThrough,
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Ahora",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = String.format(
                                        Locale.getDefault(),
                                        "%.2f",
                                        promotion.discountedPrice
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    } else {
                        Text(
                            text = String.format(
                                Locale.getDefault(),
                                "%.2f",
                                product.price
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}