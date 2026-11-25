package com.example.rider.ui

import android.media.RingtoneManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.rider.data.ThemeMode
import com.example.rider.ui.screens.ActiveTripScreen
import com.example.rider.ui.screens.AssignmentDialog
import com.example.rider.ui.screens.CashLedgerScreen
import com.example.rider.ui.screens.EarningsScreen
import com.example.rider.ui.screens.FailedDeliveryScreen
import com.example.rider.ui.screens.RiderHomeScreen
import com.example.rider.ui.screens.RiderKycScreen
import com.example.rider.ui.screens.RiderLoginScreen
import com.example.rider.ui.screens.RiderProfileScreen
import com.example.rider.ui.screens.TripHistoryScreen
import com.example.ui.theme.LocalQuickKartMetrics
import com.example.ui.theme.QuickKartTheme
import com.example.ui.theme.QuickKartThemeTokens
import com.example.ui.theme.metricsForWidth

private enum class Tab(val route: String, val label: String) { HOME("home", "Home"), EARNINGS("earnings", "Earnings"), TRIPS("trips", "Trips"), PROFILE("profile", "Profile") }

private const val ROUTE_TRIP = "trip"
private const val ROUTE_FAILED = "failed"
private const val ROUTE_CASH = "cash"
private const val ROUTE_KYC = "kyc"

@Composable
fun RiderApp(viewModel: RiderViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activity = LocalContext.current as? ComponentActivity
    val systemDark = isSystemInDarkTheme()
    val dark = when (state.settings.themeMode) { ThemeMode.LIGHT -> false; ThemeMode.DARK -> true; ThemeMode.SYSTEM -> systemDark }
    DisposableEffect(activity, dark) {
        val style = if (dark) SystemBarStyle.dark(android.graphics.Color.TRANSPARENT) else SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        activity?.enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
        onDispose { }
    }
    QuickKartTheme(darkTheme = dark, screenZoomPercent = state.settings.zoomPercent) {
        val tokens = QuickKartThemeTokens.colors
        BoxWithConstraints(Modifier.fillMaxSize().background(tokens.background)) {
            CompositionLocalProvider(LocalQuickKartMetrics provides metricsForWidth(maxWidth = maxWidth, screenZoomPercent = state.settings.zoomPercent)) {
                when {
                    !state.isReady -> Unit
                    !state.isLoggedIn -> RiderLoginScreen(onVerify = viewModel::verifyOtp)
                    !state.profile.onboarded -> RiderKycScreen(
                        profile = state.profile, onUpdate = viewModel::updateProfile, onSubmitDoc = viewModel::submitDocument,
                        onContinue = viewModel::completeOnboarding, onBack = null, onSignOut = { viewModel.signOut() }
                    )
                    else -> RiderShell(state, viewModel)
                }
            }
        }
    }
}

@Composable
private fun RiderShell(state: RiderUiState, vm: RiderViewModel) {
    val tokens = QuickKartThemeTokens.colors
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: Tab.HOME.route
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(state.message) {
        state.message?.let { snackbar.currentSnackbarData?.dismiss(); snackbar.showSnackbar(it, duration = SnackbarDuration.Short); vm.consumeMessage() }
    }
    // Ring once when a new assignment arrives.
    LaunchedEffect(state.offer?.trip?.orderId) {
        if (state.offer != null && state.settings.sound) runCatching { RingtoneManager.getRingtone(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))?.play() }
    }
    // Leave trip screens once the trip ends (delivered / cancelled / failed).
    LaunchedEffect(state.activeTrip == null) {
        if (state.activeTrip == null && (route == ROUTE_TRIP || route == ROUTE_FAILED)) nav.popBackStack(Tab.HOME.route, inclusive = false)
    }

    fun go(tab: Tab) {
        if (route == tab.route) return
        nav.navigate(tab.route) { popUpTo(Tab.HOME.route) { saveState = true }; launchSingleTop = true; restoreState = true }
    }
    fun open(r: String) = nav.navigate(r) { launchSingleTop = true }
    val back: () -> Unit = { if (!nav.popBackStack()) go(Tab.HOME) }

    val showTabs = route in Tab.entries.map { it.route }
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
                            selected = selected, onClick = { go(tab) },
                            icon = {
                                Icon(when (tab) {
                                    Tab.HOME -> if (selected) Icons.Rounded.Home else Icons.Outlined.Home
                                    Tab.EARNINGS -> if (selected) Icons.Rounded.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet
                                    Tab.TRIPS -> if (selected) Icons.Rounded.History else Icons.Outlined.History
                                    Tab.PROFILE -> if (selected) Icons.Rounded.Person else Icons.Outlined.Person
                                }, null)
                            },
                            label = { Text(tab.label, style = MaterialTheme.typography.labelSmall, maxLines = 1) },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = tokens.onBrandYellow, selectedTextColor = tokens.ink, indicatorColor = tokens.brandYellow, unselectedIconColor = tokens.muted, unselectedTextColor = tokens.muted),
                            modifier = Modifier.testTag("tab_${tab.route}")
                        )
                    }
                }
            }
        }
    ) { inner ->
        Box(Modifier.fillMaxSize().padding(inner).statusBarsPadding().then(if (showTabs) Modifier else Modifier.navigationBarsPadding())) {
            NavHost(nav, startDestination = Tab.HOME.route) {
                composable(Tab.HOME.route) {
                    RiderHomeScreen(state, onToggleOnline = vm::setOnline, onOpenTrip = { open(ROUTE_TRIP) }, onOpenCash = { open(ROUTE_CASH) },
                        onOpenKyc = { open(ROUTE_KYC) }, onRequestOffer = vm::requestOfferNow)
                }
                composable(Tab.EARNINGS.route) { EarningsScreen(state, onInstantPayout = vm::instantPayout, onOpenCash = { open(ROUTE_CASH) }) }
                composable(Tab.TRIPS.route) { TripHistoryScreen(state) }
                composable(Tab.PROFILE.route) {
                    RiderProfileScreen(state, vm::updateProfile, vm::updateSettings, onOpenKyc = { open(ROUTE_KYC) }, onSos = vm::triggerSos, onCancelSos = vm::cancelSos, onSignOut = vm::signOut)
                }
                composable(ROUTE_TRIP) {
                    val trip = state.activeTrip
                    if (trip == null) { LaunchedEffect(Unit) { back() }; return@composable }
                    ActiveTripScreen(
                        trip = trip, rider = state.position, onBack = back,
                        onArrivedStore = vm::arrivedAtStore, onTogglePicked = vm::togglePicked, onConfirmPickup = vm::confirmPickup,
                        onArrivedCustomer = vm::arrivedAtCustomer, onCalledCustomer = vm::markCalledCustomer, onPhotoTaken = vm::markPhotoTaken,
                        onCollectCash = vm::collectCash, onComplete = vm::completeDelivery, onCancel = vm::cancelTrip,
                        onReportIssue = { open(ROUTE_FAILED) }, onSkip = vm::skipTravel
                    )
                }
                composable(ROUTE_FAILED) {
                    val trip = state.activeTrip
                    if (trip == null) { LaunchedEffect(Unit) { back() }; return@composable }
                    FailedDeliveryScreen(trip, onBack = back, onSubmit = vm::reportFailedDelivery, onCall = vm::markCalledCustomer)
                }
                composable(ROUTE_CASH) { CashLedgerScreen(state, onBack = back, onDeposit = vm::depositCash) }
                composable(ROUTE_KYC) {
                    RiderKycScreen(state.profile, vm::updateProfile, vm::submitDocument, onContinue = back, onBack = back, onSignOut = { vm.signOut() })
                }
            }
        }
    }

    state.offer?.let { offer ->
        AssignmentDialog(offer, state.nowMillis, onAccept = { vm.acceptOffer(); open(ROUTE_TRIP) }, onReject = vm::rejectOffer)
    }
}
