package com.jujodevs.cursotestingandroid.productlist.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jujodevs.cursotestingandroid.R
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.FILTER_VIEW
import com.jujodevs.cursotestingandroid.productlist.domain.model.SortOption
import com.jujodevs.cursotestingandroid.productlist.presentation.ProductListAction
import com.jujodevs.cursotestingandroid.productlist.presentation.ProductListUiState

@Composable
fun FiltersMenu(
    modifier: Modifier = Modifier,
    state: ProductListUiState.Success,
    onAction: (ProductListAction) -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(FILTER_VIEW)
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.filters_categories_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.selectedCategory == null,
                    onClick = { onAction(ProductListAction.SetCategory(null)) },
                    label = { Text(
                        text = stringResource(R.string.filters_all_categories),
                        style = MaterialTheme.typography.labelSmall
                    ) }
                )
                state.categories.forEach { category ->
                    FilterChip(
                        selected = category.equals(
                            other = state.selectedCategory,
                            ignoreCase = true
                        ),
                        onClick = { onAction(ProductListAction.SetCategory(category)) },
                        label = { Text(
                            text = category,
                            style = MaterialTheme.typography.labelSmall
                        ) }
                    )
                }
            }

            HorizontalDivider()

            Text(
                text = stringResource(R.string.filters_sort_by_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OrderFilterChip(
                    text = stringResource(R.string.filters_sort_price_asc),
                    sortOption = SortOption.PRICE_ASC,
                    currentSortOption = state.sortOption,
                    onAction = onAction,
                    modifier = Modifier.weight(1f),
                )
                OrderFilterChip(
                    text = stringResource(R.string.filters_sort_price_desc),
                    sortOption = SortOption.PRICE_DESC,
                    currentSortOption = state.sortOption,
                    onAction = onAction,
                    modifier = Modifier.weight(1f),
                )
                OrderFilterChip(
                    text = stringResource(R.string.filters_sort_discount),
                    sortOption = SortOption.DISCOUNT,
                    currentSortOption = state.sortOption,
                    onAction = onAction,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
fun OrderFilterChip(
    text: String,
    sortOption: SortOption,
    currentSortOption: SortOption,
    onAction: (ProductListAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = currentSortOption == sortOption,
        onClick = { onAction(ProductListAction.SetOrderSelected(
            if (sortOption == currentSortOption) SortOption.NONE else sortOption
        )) },
        label = { Text(text = text, style = MaterialTheme.typography.labelSmall) },
        modifier = modifier,
    )
}
