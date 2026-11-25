package com.example.merchant.ui

import android.media.Ringtone
import android.media.RingtoneManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.merchant.data.ThemeMode
import com.example.merchant.ui.screens.AccountScreen
import com.example.merchant.ui.screens.AlertsZoneScreen
import com.example.merchant.ui.screens.CatalogueScreen
import com.example.merchant.ui.screens.DashboardScreen
import com.example.merchant.ui.screens.IncomingOrderDialog
import com.example.merchant.ui.screens.MerchantLoginScreen
import com.example.merchant.ui.screens.OffersScreen
import com.example.merchant.ui.screens.OrderDetailScreen
import com.example.merchant.ui.screens.OrdersFilter
import com.example.merchant.ui.screens.OrdersScreen
import com.example.merchant.ui.screens.PartialFulfilmentScreen
import com.example.merchant.ui.screens.ProductEditorScreen
import com.example.merchant.ui.screens.ROUTE_ALERTS
import com.example.merchant.ui.screens.ROUTE_CATALOGUE
import com.example.merchant.ui.screens.ROUTE_OFFERS
import com.example.merchant.ui.screens.ROUTE_REPORTS
import com.example.merchant.ui.screens.ROUTE_SETTLEMENTS
import com.example.merchant.ui.screens.ROUTE_SUPPORT
import com.example.merchant.ui.screens.ReportsScreen
import com.example.merchant.ui.screens.SettlementsScreen
import com.example.merchant.ui.screens.StoreKycScreen
import com.example.merchant.ui.screens.SupportScreen
import com.example.ui.theme.LocalQuickKartMetrics
import com.example.ui.theme.QuickKartTheme
import com.example.ui.theme.QuickKartThemeTokens
import com.example.ui.theme.metricsForWidth

private enum class Tab(val route: String, val label: String) {
    HOME("home", "Home"), ORDERS("orders", "Orders"), CATALOGUE(ROUTE_CATALOGUE, "Catalogue"), ACCOUNT("account", "Account")
}

private const val ROUTE_ORDER = "order/{id}"
private const val ROUTE_PARTIAL = "partial/{id}"
private const val ROUTE_PRODUCT = "product/{id}"
private const val ROUTE_KYC = "kyc"

@Composable
fun MerchantApp(viewModel: MerchantViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activity = LocalContext.current as? ComponentActivity
    val systemDark = isSystemInDarkTheme()
    val dark = when (state.settings.themeMode) { ThemeMode.LIGHT -> false; ThemeMode.DARK -> true; ThemeMode.SYSTEM -> systemDark }
    DisposableEffect(activity, dark) {
        val style = if (dark) SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        else SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        activity?.enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
        onDispose { }
    }

    QuickKartTheme(darkTheme = dark, screenZoomPercent = state.settings.zoomPercent) {
        val tokens = QuickKartThemeTokens.colors
        BoxWithConstraints(Modifier.fillMaxSize().background(tokens.background)) {
            CompositionLocalProvider(LocalQuickKartMetrics provides metricsForWidth(maxWidth = maxWidth, screenZoomPercent = state.settings.zoomPercent)) {
                when {
                    !state.isReady -> Unit
                    !state.isLoggedIn -> MerchantLoginScreen(onVerify = viewModel::verifyOtp)
                    !state.profile.kycSubmitted -> StoreKycScreen(
                        profile = state.profile,
                        storeOpen = state.settings.isOpen,
                        onUpdate = viewModel::updateProfile,
                        onUpload = viewModel::uploadDocument,
                        onSetStoreOpen = { viewModel.updateSettings { s -> s.copy(isOpen = it) } },
                        onSubmit = viewModel::submitKyc,
                        onBack = null,
                        onSignOut = viewModel::signOut
                    )
                    else -> MerchantShell(state, viewModel)
                }
            }
        }
    }
}

@Composable
private fun MerchantShell(state: MerchantUiState, viewModel: MerchantViewModel) {
    val tokens = QuickKartThemeTokens.colors
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: Tab.HOME.route
    val snackbar = remember { SnackbarHostState() }
    var ordersFilter by rememberSaveable { mutableStateOf(OrdersFilter.ACTIVE) }
    // Partial fulfilment hand-off from the incoming-order alert
    var partialPrep by rememberSaveable { mutableStateOf(15) }
    var partialUnavailable by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var supportOrderId by rememberSaveable { mutableStateOf<String?>(null) }
    var supportForm by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        state.message?.let { snackbar.currentSnackbarData?.dismiss(); snackbar.showSnackbar(it, duration = SnackbarDuration.Short); viewModel.consumeMessage() }
    }

    // New-order ring (system notification sound) while the alert is up.
    val context = LocalContext.current
    val incoming = state.incomingOrder
    DisposableEffect(incoming?.id, state.settings.orderSound) {
        var ringtone: Ringtone? = null
        if (incoming != null && state.settings.orderSound) {
            ringtone = runCatching { RingtoneManager.getRingtone(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)) }.getOrNull()
            runCatching { ringtone?.play() }
        }
        onDispose { runCatching { ringtone?.stop() } }
    }

    fun go(tab: Tab) {
        if (route == tab.route) return
        nav.navigate(tab.route) { popUpTo(Tab.HOME.route) { saveState = true }; launchSingleTop = true; restoreState = true }
    }
    fun open(r: String) = nav.navigate(r) { launchSingleTop = true }
    fun openSupport(orderId: String?, form: Boolean) { supportOrderId = orderId; supportForm = form; open(ROUTE_SUPPORT) }

    val tabRoutes = Tab.entries.map { it.route }
    val showTabs = route in tabRoutes
    if (showTabs && route != Tab.HOME.route) BackHandler { go(Tab.HOME) }

    Scaffold(
        containerColor = tokens.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (showTabs) {
                val inset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                NavigationBar(containerColor = tokens.surface, tonalElevation = 4.dp, modifier = Modifier.height(64.dp + inset)) {
                    Tab.entries.forEach { tab ->
                        val selected = route == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = { go(tab) },
                            icon = { TabIcon(tab, selected, newCount = state.orders.count { it.status == com.example.merchant.data.OrderStatus.NEW }) },
                            label = { Text(tab.label, style = MaterialTheme.typography.labelSmall, maxLines = 1) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = tokens.onBrandYellow, selectedTextColor = tokens.ink, indicatorColor = tokens.brandYellow,
                                unselectedIconColor = tokens.muted, unselectedTextColor = tokens.muted
                            ),
                            modifier = Modifier.testTag("tab_${tab.route}")
                        )
                    }
                }
            }
        }
    ) { inner ->
        Box(
            Modifier.fillMaxSize().padding(inner).statusBarsPadding().then(if (showTabs) Modifier else Modifier.navigationBarsPadding())
        ) {
            MerchantNavHost(nav, state, viewModel, ordersFilter, { ordersFilter = it }, ::open, { go(it) },
                onPartial = { id, prep, ids -> partialPrep = prep; partialUnavailable = ids; open("partial/$id") },
                partialPrep = partialPrep, partialUnavailable = partialUnavailable.toSet(),
                openSupport = ::openSupport, supportOrderId = supportOrderId, supportForm = supportForm)
        }
    }

    // Full-screen incoming order alert (not over the partial-fulfilment screen it leads to)
    if (incoming != null && !route.startsWith("partial")) {
        IncomingOrderDialog(
            order = incoming,
            nowMillis = state.nowMillis,
            onAccept = { prep -> viewModel.acceptOrder(incoming.id, prep) },
            onPartial = { prep, ids -> viewModel.dismissIncomingAlert(); partialPrep = prep; partialUnavailable = ids.toList(); open("partial/${incoming.id}") },
            onReject = { reason -> viewModel.rejectOrder(incoming.id, reason) },
            onLater = viewModel::dismissIncomingAlert
        )
    }
}

@Composable
private fun TabIcon(tab: Tab, selected: Boolean, newCount: Int) {
    val tokens = QuickKartThemeTokens.colors
    Box {
        Icon(
            when (tab) {
                Tab.HOME -> if (selected) Icons.Rounded.Home else Icons.Outlined.Home
                Tab.ORDERS -> if (selected) Icons.Rounded.ReceiptLong else Icons.Outlined.ReceiptLong
                Tab.CATALOGUE -> if (selected) Icons.Rounded.Inventory2 else Icons.Outlined.Inventory2
                Tab.ACCOUNT -> if (selected) Icons.Rounded.Person else Icons.Outlined.Person
            },
            contentDescription = null
        )
        if (tab == Tab.ORDERS && newCount > 0) {
            Box(
                Modifier.align(Alignment.TopEnd).padding(start = 14.dp).size(16.dp).background(tokens.ink, CircleShape),
                contentAlignment = Alignment.Center
            ) { Text("$newCount", color = tokens.background, style = MaterialTheme.typography.labelSmall) }
        }
    }
}

@Composable
private fun MerchantNavHost(
    nav: NavHostController,
    state: MerchantUiState,
    vm: MerchantViewModel,
    ordersFilter: OrdersFilter,
    setOrdersFilter: (OrdersFilter) -> Unit,
    open: (String) -> Unit,
    goTab: (Tab) -> Unit,
    onPartial: (id: String, prep: Int, ids: List<String>) -> Unit,
    partialPrep: Int,
    partialUnavailable: Set<String>,
    openSupport: (String?, Boolean) -> Unit,
    supportOrderId: String?,
    supportForm: Boolean
) {
    val back: () -> Unit = { if (!nav.popBackStack()) goTab(Tab.HOME) }
    NavHost(
        nav, startDestination = Tab.HOME.route,
        // Quick fades so two screens never visibly overlap.
        enterTransition = { androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(120)) },
        exitTransition = { androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(90)) },
        popEnterTransition = { androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(120)) },
        popExitTransition = { androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(90)) }
    ) {
        composable(Tab.HOME.route) {
            DashboardScreen(
                state = state,
                onToggleOpen = vm::setStoreOpen,
                onOpenAlerts = { open(ROUTE_ALERTS) },
                onOpenOrders = { setOrdersFilter(it); goTab(Tab.ORDERS) },
                onOpenOrder = { id -> open("order/$id") },
                onOpenKyc = { open(ROUTE_KYC) },
                onOpenTile = { r -> if (r == ROUTE_CATALOGUE) goTab(Tab.CATALOGUE) else if (r == ROUTE_SUPPORT) openSupport(null, false) else open(r) },
                onSimulateOrder = vm::simulateIncomingOrder
            )
        }
        composable(Tab.ORDERS.route) {
            OrdersScreen(state, ordersFilter, setOrdersFilter, onOpenOrder = { open("order/$it") }, onSimulateOrder = vm::simulateIncomingOrder)
        }
        composable(Tab.CATALOGUE.route) {
            CatalogueScreen(state.catalogue, onBack = null, onSetStock = vm::setStock, onEdit = { open("product/$it") }, onAdd = { open("product/new") })
        }
        composable(Tab.ACCOUNT.route) {
            AccountScreen(
                state = state,
                onEditStore = { open(ROUTE_KYC) },
                onUpdateProfile = vm::updateProfile,
                onUpdateSettings = vm::updateSettings,
                onOpen = { r -> if (r == ROUTE_SUPPORT) openSupport(null, false) else open(r) },
                onOpenSupport = { openSupport(null, true) },
                onSignOut = vm::signOut
            )
        }
        composable(ROUTE_ORDER, arguments = listOf(navArgument("id") { type = NavType.StringType })) { e ->
            val order = state.orders.firstOrNull { it.id == e.arguments?.getString("id") }
            if (order == null) { LaunchedEffect(Unit) { back() }; return@composable }
            OrderDetailScreen(
                order = order, nowMillis = state.nowMillis, onBack = back,
                onRespond = { vm.openIncomingAlert(order.id) },
                onTogglePacked = { vm.togglePacked(order.id, it) },
                onBillAttached = { vm.setBillAttached(order.id, it) },
                onBagSealed = { vm.setBagSealed(order.id, it) },
                onMarkReady = { vm.markReady(order.id) },
                onHandover = { vm.confirmHandover(order.id) },
                onCustomerCancel = { vm.simulateCustomerCancel(order.id) },
                onRaiseTicket = { openSupport(order.id, true) }
            )
        }
        composable(ROUTE_PARTIAL, arguments = listOf(navArgument("id") { type = NavType.StringType })) { e ->
            val order = state.orders.firstOrNull { it.id == e.arguments?.getString("id") }
            // If the order was missed/cancelled meanwhile, leave the screen.
            if (order == null || order.status != com.example.merchant.data.OrderStatus.NEW) { LaunchedEffect(Unit) { back() }; return@composable }
            PartialFulfilmentScreen(
                order = order, catalogue = state.catalogue, initiallyUnavailable = partialUnavailable, prepMinutes = partialPrep,
                onBack = { back(); vm.openIncomingAlert(order.id) },
                onConfirm = { lines -> vm.confirmPartialOrder(order.id, lines, partialPrep); nav.popBackStack(); open("order/${order.id}") },
                onReject = { reason -> vm.rejectOrder(order.id, reason); back() }
            )
        }
        composable(ROUTE_PRODUCT, arguments = listOf(navArgument("id") { type = NavType.StringType })) { e ->
            val id = e.arguments?.getString("id")
            val existing = state.catalogue.firstOrNull { it.id == id }
            ProductEditorScreen(existing = existing, allItems = state.catalogue, onBack = back, onSave = { vm.saveProduct(it); back() }, onDelete = vm::deleteProduct)
        }
        composable(ROUTE_KYC) {
            StoreKycScreen(
                profile = state.profile, storeOpen = state.settings.isOpen, onUpdate = vm::updateProfile, onUpload = vm::uploadDocument,
                onSetStoreOpen = vm::setStoreOpen, onSubmit = { vm.submitKyc(); back() }, onBack = back, onSignOut = vm::signOut
            )
        }
        composable(ROUTE_OFFERS) { OffersScreen(state.offers, state.nowMillis, back, vm::setOfferActive, vm::createOffer, vm::deleteOffer) }
        composable(ROUTE_SETTLEMENTS) { SettlementsScreen(state, back, onRaiseTicket = { openSupport(null, true) }) }
        composable(ROUTE_REPORTS) { ReportsScreen(state, back) }
        composable(ROUTE_ALERTS) {
            AlertsZoneScreen(
                state = state, onBack = back,
                onOpenAlert = { a -> vm.markAlertRead(a.id); a.orderId?.let { id -> if (state.orders.any { it.id == id }) open("order/$id") } },
                onMarkAllRead = vm::markAllAlertsRead,
                onUpdateSettings = vm::updateSettings
            )
        }
        composable(ROUTE_SUPPORT) { SupportScreen(state, supportOrderId, supportForm, back, vm::raiseTicket) }
    }
}
