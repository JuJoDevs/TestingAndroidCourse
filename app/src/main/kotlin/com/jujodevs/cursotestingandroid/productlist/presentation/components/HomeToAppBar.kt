package com.jujodevs.cursotestingandroid.productlist.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.jujodevs.cursotestingandroid.productlist.presentation.ProductListAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    filterVisible: Boolean,
    onAction: (ProductListAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = "MiniMarket",
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
                onClick = { onAction(ProductListAction.SetFiltersVisible(!filterVisible)) }
            ) {
                Icon(
                    imageVector =
                        if (filterVisible) Icons.Default.FilterListOff
                        else Icons.Default.FilterList,
                    contentDescription =
                        if (filterVisible) "Ocultar filtros"
                        else "Mostrar filtros",
                )
            }
            IconButton(
                onClick = { onAction(ProductListAction.NavToSettings) }
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Ir a opciones"
                )
            }
        }
    )
}
