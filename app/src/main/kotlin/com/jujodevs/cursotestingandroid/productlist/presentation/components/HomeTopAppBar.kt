package com.jujodevs.cursotestingandroid.productlist.presentation.components

import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jujodevs.cursotestingandroid.R
import com.jujodevs.cursotestingandroid.core.test.UiTestTag
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.TOP_APP_BAR_BADGE
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.TOP_APP_BAR_CART
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.TOP_APP_BAR_FILTER
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.TOP_APP_BAR_SETTINGS
import com.jujodevs.cursotestingandroid.productlist.presentation.ProductListAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    filterVisible: Boolean,
    cartItemCount: Int,
    onAction: (ProductListAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = stringResource(R.string.app_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        actions = {
            IconButton(
                modifier = Modifier.testTag(TOP_APP_BAR_FILTER),
                onClick = { onAction(ProductListAction.SetFiltersVisible(!filterVisible)) }
            ) {
                Icon(
                    imageVector =
                        if (filterVisible) Icons.Default.FilterListOff
                        else Icons.Default.FilterList,
                    contentDescription =
                        if (filterVisible) stringResource(R.string.top_app_bar_hide_filters)
                        else stringResource(R.string.top_app_bar_show_filters),
                )
            }
            IconButton(
                modifier = Modifier.testTag(TOP_APP_BAR_SETTINGS),
                onClick = { onAction(ProductListAction.NavToSettings) }
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.top_app_bar_go_to_settings)
                )
            }
            BadgedBox(
                badge = {
                    if (cartItemCount > 0) {
                        Badge(
                            modifier = Modifier
                                .testTag(TOP_APP_BAR_BADGE)
                                .offset(x = (-8).dp, y = (8).dp)
                        ) {
                            Text(
                                text =
                                    if (cartItemCount > 99)
                                        stringResource(R.string.top_app_bar_badge_ninety_nine_plus)
                                    else cartItemCount.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            ) {
                IconButton(
                    modifier = Modifier.testTag(TOP_APP_BAR_CART),
                    onClick = { onAction(ProductListAction.NavToCart) }
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = stringResource(R.string.top_app_bar_go_to_cart)
                    )
                }
            }
        }
    )
}
