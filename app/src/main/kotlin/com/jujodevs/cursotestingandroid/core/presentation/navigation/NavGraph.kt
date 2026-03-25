package com.jujodevs.cursotestingandroid.core.presentation.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.jujodevs.cursotestingandroid.cart.presentation.CartScreen
import com.jujodevs.cursotestingandroid.detail.presentation.ProductDetailScreen
import com.jujodevs.cursotestingandroid.productlist.presentation.ProductListScreen
import com.jujodevs.cursotestingandroid.settings.presentation.SettingsScreen

@Composable
fun NavGraph() {
    val backStack = rememberNavBackStack(Screen.ProductList)
    val entries = entryProvider<NavKey> {
        entry<Screen.ProductList> {
            ProductListScreen(
                navigateToSettings = { backStack.add(Screen.Settings) },
                navigateToProductDetail = { backStack.add(Screen.ProductDetail(it)) },
                navigateToCart = { backStack.add(Screen.Cart) },
            )
        }
        entry<Screen.Cart> {
            CartScreen(
                onBack = { backStack.removeLastOrNull() }
            )
        }
        entry<Screen.Settings> {
            SettingsScreen(
                onBack = { backStack.removeLastOrNull() }
            )
        }
        entry<Screen.ProductDetail> { route ->
            ProductDetailScreen(
                productId = route.productId,
                onBack = { backStack.removeLastOrNull() }
            )
        }
    }
    NavDisplay(
        backStack = backStack,
        entryProvider = entries,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        transitionSpec = {
            slideInHorizontally(
                initialOffsetX = { it / 4 },
            ) + fadeIn() togetherWith
                    slideOutHorizontally(
                        targetOffsetX = { -it / 4 },
                    ) + fadeOut()
        },
        popTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { -it / 4 },
            ) + fadeIn() togetherWith
                    slideOutHorizontally(
                        targetOffsetX = { it / 4 },
                    ) + fadeOut()
        },
        predictivePopTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { -it / 4 },
            ) + fadeIn() togetherWith
                    slideOutHorizontally(
                        targetOffsetX = { it / 4 },
                    ) + fadeOut()
        },
    )
}
