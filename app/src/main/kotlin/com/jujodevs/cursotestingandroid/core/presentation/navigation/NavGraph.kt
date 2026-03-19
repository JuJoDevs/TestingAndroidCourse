package com.jujodevs.cursotestingandroid.core.presentation.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.jujodevs.cursotestingandroid.productlist.presentation.ProductListScreen

@Composable
fun NavGraph(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(Screen.ProductList)
    val entries = entryProvider<NavKey> {
        entry<Screen.ProductList> {
            ProductListScreen()
        }
        entry<Screen.Cart> {
            Text("Cart", fontSize = 30.sp)
        }
        entry<Screen.Settings> {
            Text("Settings", fontSize = 30.sp)
        }
        entry<Screen.ProductDetail> {
            Text("ProductDetail", fontSize = 30.sp)
        }
    }
    NavDisplay(
        backStack = backStack,
        entryProvider = entries,
        onBack = { backStack.removeLastOrNull() }
    )
}
