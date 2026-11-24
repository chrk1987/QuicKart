package com.example.ui

import com.example.ui.screens.LocalSavedPayments
import com.example.ui.screens.SavedPayments

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import com.example.util.itemCountLabel
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
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ReceiptLong
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
import com.example.ui.screens.AccountSubScreenTopBar
import com.example.ui.screens.CartCheckoutScreen
import com.example.ui.screens.LiveTrackingScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.OrderHistoryScreen
import com.example.ui.screens.StorefrontScreen
import com.example.ui.screens.SupportScreen
import androidx.compose.runtime.CompositionLocalProvider
import com.example.data.model.OrderRecord
import com.example.ui.components.OrderPlacedCelebrationOverlay
import com.example.ui.components.QuickKartLogo
import com.example.ui.theme.LocalQuickKartMetrics
import com.example.ui.theme.QuickKartMetricsTokens
import com.example.ui.theme.QuickKartTheme
import com.example.ui.theme.QuickKartThemeTokens
import com.example.ui.theme.metricsForWidth
import kotlinx.coroutines.launch

const val ROUTE_SUPPORT = "support"
const val ROUTE_ACCOUNT_SECTION = "account/{section}"

/** Screens pushed on top of a tab: they show a back arrow and hide the bottom navigation. */
private val SUB_SCREEN_ROUTES = setOf(ROUTE_SUPPORT, ROUTE_ACCOUNT_SECTION, BottomTab.ORDERS.route)

@Composable
fun QuickKartApp(
    viewModel: QuickKartViewModel,
    initialDestination: String? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Status/navigation bar icons follow the app's own Light/Dark setting (not the phone's),
    // with transparent bars so no grey scrim is drawn over the app.
    val activity = LocalContext.current as? ComponentActivity
    val isDarkTheme = uiState.preferences.isDarkTheme
    DisposableEffect(activity, isDarkTheme) {
        val barStyle = if (isDarkTheme) {
            SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        } else {
            SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        }
        activity?.enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
        onDispose { }
    }

    QuickKartTheme(
        darkTheme = uiState.preferences.isDarkTheme,
        screenZoomPercent = uiState.preferences.screenZoomPercent
    ) {
        val tokens = QuickKartThemeTokens.colors
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        var celebratingOrder by remember { mutableStateOf<OrderRecord?>(null) }

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
            val metrics = metricsForWidth(
                maxWidth = maxWidth,
                screenZoomPercent = uiState.preferences.screenZoomPercent
            )
            CompositionLocalProvider(
                LocalQuickKartMetrics provides metrics,
                LocalSavedPayments provides SavedPayments(
                    upiId = uiState.preferences.savedUpiId,
                    cardLast4 = uiState.preferences.savedCardLast4,
                    onSave = viewModel::savePaymentMethod
                )
            ) {
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
                        onSignInWithPhone = { phone, nId, fName, lName, email ->
                            viewModel.signInWithPhone(phone, nId, fName, lName, email)
                        },
                        onContinueAsGuest = viewModel::continueAsGuest,
                        onSignInWithGoogle = { phone, nId, fName, lName, email, googleEmail ->
                            viewModel.signInWithPhone(phone, nId, fName, lName, email, googleEmail)
                        }
                    )
                    return@CompositionLocalProvider
                }

        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: BottomTab.HOME.route
        var supportPreselectedOrderId by rememberSaveable { mutableStateOf<String?>(null) }
        var openWizardOnSupport by rememberSaveable { mutableStateOf(false) }

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
                // Orders is no longer a footer tab; it is reached from Account.
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

        fun openSupport(orderId: String? = null, openWizardDirectly: Boolean = false) {
            supportPreselectedOrderId = orderId
            openWizardOnSupport = openWizardDirectly
            if (currentRoute != ROUTE_SUPPORT) {
                navController.navigate(ROUTE_SUPPORT) {
                    launchSingleTop = true
                }
            }
        }

        // Sub-screens use the default back behaviour (pop to the previous screen).
        if (currentRoute != BottomTab.HOME.route && currentRoute !in SUB_SCREEN_ROUTES) {
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
                val showTopLevelNav = currentRoute !in SUB_SCREEN_ROUTES

            Scaffold(
                containerColor = tokens.background,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    if (showTopLevelNav && !useNavRail) {
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
                // When the bottom tab bar is hidden (sub-screens, nav-rail layouts) nothing else
                // reserves the system navigation bar area, so pad for it here.
                val bottomBarVisible = showTopLevelNav && !useNavRail
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .statusBarsPadding()
                        .then(if (bottomBarVisible) Modifier else Modifier.navigationBarsPadding())
                ) {
                    if (showTopLevelNav && useNavRail) {
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

                    // Shared by the Account list and each Account section screen.
                    val accountScreen: @Composable (String?) -> Unit = { openSection ->
                        AccountHistoryScreen(
                            uiState = uiState,
                            onToggleTheme = viewModel::toggleDarkTheme,
                            onToggleNotifications = viewModel::toggleNotifications,
                            onScreenZoomChange = viewModel::setScreenZoomPercent,
                            onSetDefaultAddress = viewModel::setDefaultAddress,
                            onDeleteAddress = viewModel::deleteAddress,
                            onAddAddress = viewModel::addAddress,
                            onAddWalletMoney = viewModel::addWalletMoney,
                            onUpdateProfile = { fName, lName, email ->
                                viewModel.updateUserProfile(fName, lName, email)
                            },
                            onOpenOrderHistory = {
                                navController.navigate(BottomTab.ORDERS.route) { launchSingleTop = true }
                            },
                            onOpenSupportHub = {
                                openSupport(orderId = null, openWizardDirectly = false)
                            },
                            onSignOut = viewModel::signOut,
                            onShowMessage = { msg -> showToast(msg) },
                            onLinkGoogleAccount = viewModel::linkGoogleAccount,
                            onUnlinkGoogleAccount = viewModel::unlinkGoogleAccount,
                            promos = viewModel.availablePromos,
                            onRedeemReferral = { code ->
                                viewModel.redeemReferralCode(code, referralCodeFor(uiState.preferences.userPhone))
                            },
                            onRemoveSavedPayments = viewModel::removeSavedPaymentMethods,
                            openSection = openSection,
                            onOpenSection = { section ->
                                navController.navigate("account/$section") { launchSingleTop = true }
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )
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
                                onToggleFavoritesFilter = viewModel::toggleFavoritesFilter,
                                onSetFavoritesOnly = viewModel::setFavoritesOnly,
                                onQuickAddAllFavorites = {
                                    viewModel.quickAddAllFavorites { count ->
                                        if (count > 0) {
                                            showToast(
                                                message = "Quick-added $count favorite items to Cart",
                                                actionLabel = "View Cart",
                                                onAction = { navigateToTab(BottomTab.CART) }
                                            )
                                        }
                                    }
                                },
                                onSearchQueryChange = viewModel::updateSearchQuery,
                                onVoiceListeningChange = viewModel::setVoiceListening,
                                onToggleFavorite = viewModel::toggleFavorite,
                                onIncrementCart = viewModel::incrementCart,
                                onDecrementCart = viewModel::decrementCart,
                                onOpenCart = { navigateToTab(BottomTab.CART) },
                                onShowMessage = { msg -> showToast(msg) },
                                onSearchSubmitted = viewModel::addRecentSearch,
                                onClearRecentSearches = viewModel::clearRecentSearches,
                                onMarkInboxSeen = viewModel::markInboxSeen
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
                                onApplyBestOffer = viewModel::applyBestOffer,
                                onSelectTip = viewModel::selectTip,
                                onSelectCustomTipMode = viewModel::selectCustomTipMode,
                                onCustomTipChange = viewModel::updateCustomTip,
                                onToggleInstruction = viewModel::toggleDeliveryInstruction,
                                onRiderNoteChange = viewModel::updateRiderNote,
                                onSelectPaymentMethod = viewModel::selectPaymentMethod,
                                onSelectDeliverySlot = viewModel::selectDeliverySlot,
                                onAddWalletMoney = { amt, methodNote ->
                                    viewModel.addWalletMoney(amt, methodNote)
                                    showToast("Added ₹$amt to QuickKart Wallet!")
                                },
                                onPlaceOrder = {
                                    viewModel.placeOrder { order ->
                                        celebratingOrder = order
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
                                onOpenSupport = { targetOrder ->
                                    openSupport(orderId = targetOrder.orderId, openWizardDirectly = true)
                                },
                                onBrowseStorefront = { navigateToTab(BottomTab.HOME) },
                                onShowMessage = { msg -> showToast(msg) },
                                onCancelOrder = { order ->
                                    viewModel.cancelOrder(order) { msg -> showToast(msg) }
                                }
                            )
                        }

                        composable(BottomTab.ORDERS.route) {
                            Column(modifier = Modifier.fillMaxSize().background(tokens.background)) {
                            // Order History draws its own title header, so the bar shows only the back arrow.
                            AccountSubScreenTopBar(
                                title = "",
                                onBack = { navController.popBackStack() },
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            Box(modifier = Modifier.weight(1f)) {
                            OrderHistoryScreen(
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
                                        message = "Added ${itemCountLabel(order.totalItemCount)} to Cart",
                                        actionLabel = "View Cart",
                                        onAction = { navigateToTab(BottomTab.CART) }
                                    )
                                },
                                onReorderCheckout = { order ->
                                    viewModel.reorderAndCheckout(order) {
                                        navigateToTab(BottomTab.CART)
                                    }
                                },
                                onSubmitRating = viewModel::submitRating,
                                onOpenSupportForOrder = { targetOrder ->
                                    openSupport(orderId = targetOrder.orderId, openWizardDirectly = true)
                                },
                                onShowMessage = { msg -> showToast(msg) }
                            )
                            }
                            }
                        }

                        composable(BottomTab.ACCOUNT.route) {
                            accountScreen(null)
                        }

                        // Each Account parent setting opens here as its own screen.
                        composable(ROUTE_ACCOUNT_SECTION) { entry ->
                            accountScreen(entry.arguments?.getString("section"))
                        }

                        composable(ROUTE_SUPPORT) {
                            SupportScreen(
                                orders = uiState.orders,
                                tickets = uiState.supportTickets,
                                initialOrderId = supportPreselectedOrderId,
                                openWizardInitially = openWizardOnSupport,
                                onCreateTicket = viewModel::createSupportTicket,
                                onSendReply = viewModel::sendSupportMessage,
                                onResolveTicket = viewModel::resolveSupportTicket,
                                onReopenTicket = viewModel::reopenSupportTicket,
                                onSubmitCsat = viewModel::submitSupportCsat,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onShowMessage = { msg -> showToast(msg) }
                            )
                        }
                    }
                }
            }

            OrderPlacedCelebrationOverlay(
                order = celebratingOrder,
                onFinished = {
                    val placed = celebratingOrder
                    celebratingOrder = null
                    if (placed != null) {
                        navigateToTab(BottomTab.TRACK)
                        showToast("Order ${placed.orderId} placed! Arriving in ${placed.etaMinutes}m")
                    }
                }
            )
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
        BottomTab.ORDERS -> if (selected) Icons.Rounded.ReceiptLong else Icons.Outlined.ReceiptLong
        BottomTab.ACCOUNT -> if (selected) Icons.Rounded.Person else Icons.Outlined.Person
    }

    when (tab) {
        BottomTab.CART -> {
            BadgedBox(
                badge = {
                    if (cartCount > 0) {
                        Badge(
                            containerColor = tokens.deliveryGreenDeep,
                            contentColor = Color.White,
                            // Sit on the icon's top-right corner instead of covering the cart.
                            modifier = Modifier.offset(x = 6.dp, y = (-4).dp)
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
