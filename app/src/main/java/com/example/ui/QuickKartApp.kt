package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeliveryDining
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AccountHistoryScreen
import com.example.ui.screens.CartCheckoutScreen
import com.example.ui.screens.LiveTrackingScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.StorefrontScreen
import androidx.compose.runtime.CompositionLocalProvider
import com.example.ui.components.QuickKartLogo
import com.example.ui.theme.LocalQuickKartMetrics
import com.example.ui.theme.QuickKartMetricsTokens
import com.example.ui.theme.QuickKartTheme
import com.example.ui.theme.QuickKartThemeTokens
import com.example.ui.theme.metricsForWidth
import kotlinx.coroutines.launch

@Composable
fun QuickKartApp(
    viewModel: QuickKartViewModel,
    initialDestination: String? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    QuickKartTheme(darkTheme = uiState.preferences.isDarkTheme) {
        val tokens = QuickKartThemeTokens.colors
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        fun showToast(message: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
            scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                val res = snackbarHostState.showSnackbar(
                    message = message,
                    actionLabel = actionLabel,
                    duration = SnackbarDuration.Short
                )
                if (res == SnackbarResult.ActionPerformed) {
                    onAction?.invoke()
                }
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(tokens.background)
        ) {
            val metrics = metricsForWidth(maxWidth)
            CompositionLocalProvider(LocalQuickKartMetrics provides metrics) {
                if (!uiState.isReady) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(tokens.background),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            QuickKartLogo(
                                size = metrics.avatarSize + 14.dp,
                                cornerRadius = 18.dp
                            )
                            Text(
                                text = "QuickKart",
                                style = MaterialTheme.typography.titleLarge,
                                color = tokens.ink
                            )
                            CircularProgressIndicator(color = tokens.brandYellow)
                        }
                    }
                    return@CompositionLocalProvider
                }

                if (!uiState.preferences.isLoggedIn) {
                    LoginScreen(
                        neighborhoods = uiState.neighborhoods,
                        selectedNeighborhoodId = uiState.preferences.selectedNeighborhoodId,
                        isDarkTheme = uiState.preferences.isDarkTheme,
                        onToggleTheme = viewModel::toggleDarkTheme,
                        onSelectNeighborhood = viewModel::selectNeighborhood,
                        onSignInWithPhone = viewModel::signInWithPhone,
                        onContinueAsGuest = viewModel::continueAsGuest
                    )
                    return@CompositionLocalProvider
                }

        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: BottomTab.HOME.route

        var handledInitialNav by rememberSaveable { mutableStateOf(false) }
        LaunchedEffect(initialDestination, uiState.isReady) {
            if (!handledInitialNav && initialDestination == "track") {
                handledInitialNav = true
                navController.navigate(BottomTab.TRACK.route) {
                    launchSingleTop = true
                }
            }
        }

        val showTrackTab = uiState.activeOrder != null || currentRoute == BottomTab.TRACK.route
        val visibleTabs = remember(showTrackTab) {
            buildList {
                add(BottomTab.HOME)
                add(BottomTab.CART)
                if (showTrackTab) add(BottomTab.TRACK)
                add(BottomTab.ACCOUNT)
            }
        }

        fun navigateToTab(tab: BottomTab) {
            if (currentRoute == tab.route) return
            navController.navigate(tab.route) {
                popUpTo(BottomTab.HOME.route) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }

        if (currentRoute != BottomTab.HOME.route) {
            BackHandler {
                navigateToTab(BottomTab.HOME)
            }
        }

        val pulseTransition = rememberInfiniteTransition(label = "track_pulse")
        val pulseScale by pulseTransition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.35f,
            animationSpec = infiniteRepeatable(
                animation = tween(750),
                repeatMode = RepeatMode.Reverse
            ),
            label = "track_pulse_scale"
        )

                val useNavRail = metrics.useNavRail

            Scaffold(
                containerColor = tokens.background,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    if (!useNavRail) {
                        NavigationBar(
                            containerColor = tokens.surface,
                            tonalElevation = 4.dp
                        ) {
                            visibleTabs.forEach { tab ->
                                val selected = currentRoute == tab.route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateToTab(tab) },
                                    icon = {
                                        TabNavIcon(
                                            tab = tab,
                                            selected = selected,
                                            cartCount = uiState.totalCartCount,
                                            hasActiveOrder = uiState.activeOrder != null,
                                            pulseScale = pulseScale
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = tokens.onBrandYellow,
                                        selectedTextColor = tokens.ink,
                                        indicatorColor = tokens.brandYellow,
                                        unselectedIconColor = tokens.muted,
                                        unselectedTextColor = tokens.muted
                                    ),
                                    modifier = Modifier.testTag("bottom_tab_${tab.route}")
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .statusBarsPadding()
                ) {
                    if (useNavRail) {
                        NavigationRail(
                            containerColor = tokens.surface,
                            header = {
                                Spacer(Modifier.height(8.dp))
                                QuickKartLogo(
                                    size = metrics.badgeButtonSize,
                                    cornerRadius = 12.dp
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                        ) {
                            visibleTabs.forEach { tab ->
                                val selected = currentRoute == tab.route
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateToTab(tab) },
                                    icon = {
                                        TabNavIcon(
                                            tab = tab,
                                            selected = selected,
                                            cartCount = uiState.totalCartCount,
                                            hasActiveOrder = uiState.activeOrder != null,
                                            pulseScale = pulseScale
                                        )
                                    },
                                    label = { Text(tab.label, maxLines = 1, softWrap = false) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = tokens.onBrandYellow,
                                        selectedTextColor = tokens.ink,
                                        indicatorColor = tokens.brandYellow,
                                        unselectedIconColor = tokens.muted,
                                        unselectedTextColor = tokens.muted
                                    ),
                                    modifier = Modifier.testTag("rail_tab_${tab.route}")
                                )
                            }
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = BottomTab.HOME.route,
                        modifier = Modifier.weight(1f)
                    ) {
                        composable(BottomTab.HOME.route) {
                            StorefrontScreen(
                                uiState = uiState,
                                onSelectNeighborhood = viewModel::selectNeighborhood,
                                onSelectCategory = viewModel::selectCategory,
                                onSearchQueryChange = viewModel::updateSearchQuery,
                                onVoiceListeningChange = viewModel::setVoiceListening,
                                onToggleFavorite = viewModel::toggleFavorite,
                                onIncrementCart = viewModel::incrementCart,
                                onDecrementCart = viewModel::decrementCart,
                                onOpenCart = { navigateToTab(BottomTab.CART) },
                                onShowMessage = { msg -> showToast(msg) }
                            )
                        }

                        composable(BottomTab.CART.route) {
                            CartCheckoutScreen(
                                uiState = uiState,
                                onSelectAddress = viewModel::selectAddress,
                                onAddAddress = viewModel::addAddress,
                                onIncrementCart = viewModel::incrementCart,
                                onDecrementCart = viewModel::decrementCart,
                                onRemoveCartItem = viewModel::removeCartItem,
                                onPromoInputChange = viewModel::updatePromoInput,
                                onApplyPromo = viewModel::applyPromoCode,
                                onRemovePromo = viewModel::removePromo,
                                onSelectTip = viewModel::selectTip,
                                onSelectCustomTipMode = viewModel::selectCustomTipMode,
                                onCustomTipChange = viewModel::updateCustomTip,
                                onToggleInstruction = viewModel::toggleDeliveryInstruction,
                                onRiderNoteChange = viewModel::updateRiderNote,
                                onSelectPaymentMethod = viewModel::selectPaymentMethod,
                                onPlaceOrder = {
                                    viewModel.placeOrder { order ->
                                        showToast("Order ${order.orderId} placed! Arriving in ${order.etaMinutes}m")
                                        navigateToTab(BottomTab.TRACK)
                                    }
                                },
                                onBrowseStorefront = { navigateToTab(BottomTab.HOME) }
                            )
                        }

                        composable(BottomTab.TRACK.route) {
                            LiveTrackingScreen(
                                order = uiState.latestTrackableOrder,
                                notificationsEnabled = uiState.preferences.notificationsEnabled,
                                autoSimulating = uiState.autoSimulateTracking,
                                onAdvanceStage = viewModel::advanceOrderStage,
                                onRevertStage = viewModel::revertOrderStage,
                                onToggleAutoSim = viewModel::toggleAutoSimulate,
                                onSubmitRating = viewModel::submitRating,
                                onBrowseStorefront = { navigateToTab(BottomTab.HOME) },
                                onShowMessage = { msg -> showToast(msg) }
                            )
                        }

                        composable(BottomTab.ACCOUNT.route) {
                            AccountHistoryScreen(
                                uiState = uiState,
                                onAddRestockItem = { suggestion ->
                                    viewModel.addProductToCart(suggestion.product)
                                    showToast(
                                        message = "Added ${suggestion.product.name} to Cart",
                                        actionLabel = "View Cart",
                                        onAction = { navigateToTab(BottomTab.CART) }
                                    )
                                },
                                onHistorySearchChange = viewModel::updateHistorySearch,
                                onSelectTimeFilter = viewModel::selectHistoryTimeFilter,
                                onQuickReorder = { order ->
                                    viewModel.quickReorder(order)
                                    showToast(
                                        message = "Added ${order.totalItemCount} items to Cart",
                                        actionLabel = "View Cart",
                                        onAction = { navigateToTab(BottomTab.CART) }
                                    )
                                },
                                onReorderCheckout = { order ->
                                    viewModel.reorderAndCheckout(order) {
                                        navigateToTab(BottomTab.CART)
                                    }
                                },
                                onToggleTheme = viewModel::toggleDarkTheme,
                                onToggleNotifications = viewModel::toggleNotifications,
                                onSetDefaultAddress = viewModel::setDefaultAddress,
                                onDeleteAddress = viewModel::deleteAddress,
                                onAddAddress = viewModel::addAddress,
                                onSignOut = viewModel::signOut,
                                onShowMessage = { msg -> showToast(msg) }
                            )
                        }
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun TabNavIcon(
    tab: BottomTab,
    selected: Boolean,
    cartCount: Int,
    hasActiveOrder: Boolean,
    pulseScale: Float
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    val icon = when (tab) {
        BottomTab.HOME -> if (selected) Icons.Rounded.Home else Icons.Outlined.Home
        BottomTab.CART -> if (selected) Icons.Rounded.ShoppingCart else Icons.Outlined.ShoppingCart
        BottomTab.TRACK -> if (selected) Icons.Rounded.DeliveryDining else Icons.Outlined.DeliveryDining
        BottomTab.ACCOUNT -> if (selected) Icons.Rounded.Person else Icons.Outlined.Person
    }

    when (tab) {
        BottomTab.CART -> {
            BadgedBox(
                badge = {
                    if (cartCount > 0) {
                        Badge(
                            containerColor = tokens.deliveryGreenDeep,
                            contentColor = Color.White
                        ) {
                            Text(cartCount.toString())
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = tab.label,
                    modifier = Modifier.size(metrics.navIconSize)
                )
            }
        }

        BottomTab.TRACK -> {
            Box {
                Icon(
                    imageVector = icon,
                    contentDescription = tab.label,
                    modifier = Modifier.size(metrics.navIconSize)
                )
                if (hasActiveOrder) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-2).dp)
                            .size(9.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(tokens.deliveryGreen)
                    )
                }
            }
        }

        else -> Icon(
            imageVector = icon,
            contentDescription = tab.label,
            modifier = Modifier.size(metrics.navIconSize)
        )
    }
}
