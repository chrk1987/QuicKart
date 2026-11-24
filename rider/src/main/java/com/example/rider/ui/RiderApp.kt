package com.example.rider.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.DeliveryDining
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rider.ui.components.DeliveryCompleteOverlay
import com.example.rider.ui.screens.ActiveTripScreen
import com.example.rider.ui.screens.DutyDashboardScreen
import com.example.rider.ui.screens.EarningsScreen
import com.example.rider.ui.screens.RiderAuthScreen
import com.example.rider.ui.screens.RiderProfileScreen
import com.example.rider.ui.theme.LocalRiderMetrics
import com.example.rider.ui.theme.QuickKartRiderTheme
import com.example.rider.ui.theme.RiderThemeTokens
import com.example.rider.ui.theme.metricsForWidth

private enum class RiderTab(
    val routeId: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DUTY("duty_hub", "Duty", Icons.Rounded.Radar, Icons.Outlined.Radar),
    ACTIVE_TRIP("active_trip", "Run", Icons.Rounded.DeliveryDining, Icons.Outlined.DeliveryDining),
    EARNINGS("earnings", "Earnings", Icons.Rounded.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
    PROFILE("profile", "Profile", Icons.Rounded.Person, Icons.Outlined.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiderApp(
    viewModel: RiderViewModel,
    initialDestination: String? = null
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val celebration by viewModel.celebration.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentTab by rememberSaveable {
        mutableStateOf(
            if (initialDestination == "active_trip") RiderTab.ACTIVE_TRIP else RiderTab.DUTY
        )
    }

    LaunchedEffect(viewModel) {
        viewModel.snackbarMessages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.navigateToActiveTripEvent.collect {
            currentTab = RiderTab.ACTIVE_TRIP
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val responsiveMetrics = remember(maxWidth, state.prefs.screenZoomPercent) {
            metricsForWidth(
                maxWidth = maxWidth,
                screenZoomPercent = state.prefs.screenZoomPercent
            )
        }

        CompositionLocalProvider(LocalRiderMetrics provides responsiveMetrics) {
            QuickKartRiderTheme(
                darkTheme = state.prefs.isDarkTheme,
                screenZoomPercent = state.prefs.screenZoomPercent
            ) {
                val tokens = RiderThemeTokens.colors

                if (!state.prefs.isLoggedIn) {
                    RiderAuthScreen(
                        hubs = state.availableHubs,
                        initialName = state.prefs.riderName,
                        initialPhone = state.prefs.riderPhone,
                        initialFleetId = state.prefs.fleetId,
                        initialVehicle = state.prefs.vehicleType,
                        initialHubId = state.prefs.hubId,
                        onStartShift = viewModel::loginRider
                    )
                } else {
                    if (currentTab != RiderTab.DUTY) {
                        BackHandler { currentTab = RiderTab.DUTY }
                    }

                    val topTitle = when (currentTab) {
                        RiderTab.DUTY -> "QK Rider • Duty Hub"
                        RiderTab.ACTIVE_TRIP -> state.activeTrip?.let { "Run • ${it.orderId}" } ?: "Active Run"
                        RiderTab.EARNINGS -> "Earnings & COD Ledger"
                        RiderTab.PROFILE -> "Partner Profile"
                    }

                    Scaffold(
                        containerColor = tokens.background,
                        contentWindowInsets = WindowInsets.safeDrawing,
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Text(
                                        text = topTitle,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                },
                                actions = {
                                    Surface(
                                        color = if (state.prefs.isOnline) {
                                            tokens.deliveryGreenSoft
                                        } else {
                                            tokens.surfaceElevated
                                        },
                                        shape = MaterialTheme.shapes.small,
                                        modifier = Modifier.padding(end = 12.dp)
                                    ) {
                                        Text(
                                            text = if (state.prefs.isOnline) "ONLINE" else "OFFLINE",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (state.prefs.isOnline) {
                                                tokens.deliveryGreenDeep
                                            } else {
                                                tokens.muted
                                            },
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = tokens.surface,
                                    titleContentColor = tokens.ink
                                )
                            )
                        },
                        bottomBar = {
                            if (!responsiveMetrics.useNavRail) {
                                NavigationBar(containerColor = tokens.surface) {
                                    RiderTab.entries.forEach { tab ->
                                        val selected = currentTab == tab
                                        val showBadge = (tab == RiderTab.ACTIVE_TRIP && state.activeTrip != null) ||
                                            (tab == RiderTab.DUTY && state.pendingOffer != null)
                                        NavigationBarItem(
                                            selected = selected,
                                            onClick = { currentTab = tab },
                                            icon = {
                                                BadgedBox(
                                                    badge = {
                                                        if (showBadge) {
                                                            Badge(containerColor = tokens.deliveryGreenDeep)
                                                        }
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                        contentDescription = tab.label,
                                                        modifier = Modifier.size(responsiveMetrics.navIconSize)
                                                    )
                                                }
                                            },
                                            label = { Text(tab.label) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = tokens.deliveryGreenDeep,
                                                selectedTextColor = tokens.deliveryGreenDeep,
                                                indicatorColor = tokens.deliveryGreenSoft
                                            ),
                                            modifier = Modifier.testTag("nav_tab_${tab.routeId}")
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
                        ) {
                            if (responsiveMetrics.useNavRail) {
                                NavigationRail(containerColor = tokens.surface) {
                                    RiderTab.entries.forEach { tab ->
                                        val selected = currentTab == tab
                                        NavigationRailItem(
                                            selected = selected,
                                            onClick = { currentTab = tab },
                                            icon = {
                                                Icon(
                                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                    contentDescription = tab.label
                                                )
                                            },
                                            label = { Text(tab.label) },
                                            modifier = Modifier.testTag("rail_tab_${tab.routeId}")
                                        )
                                    }
                                }
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                when (currentTab) {
                                    RiderTab.DUTY -> DutyDashboardScreen(
                                        state = state,
                                        onToggleOnline = viewModel::toggleOnline,
                                        onSelectHub = viewModel::selectHub,
                                        onAcceptOffer = viewModel::acceptOffer,
                                        onDeclineOffer = viewModel::declineOffer,
                                        onPingNewOffer = viewModel::pingNewOrderOffer,
                                        onOpenActiveTrip = { currentTab = RiderTab.ACTIVE_TRIP },
                                        onOpenEarnings = { currentTab = RiderTab.EARNINGS }
                                    )

                                    RiderTab.ACTIVE_TRIP -> ActiveTripScreen(
                                        activeTrip = state.activeTrip,
                                        otpError = state.otpError,
                                        onArrivedAtHub = viewModel::markArrivedAtHub,
                                        onToggleItem = viewModel::togglePickedItem,
                                        onPickAllAndSeal = viewModel::pickAllAndSeal,
                                        onToggleColdSeal = viewModel::toggleColdSeal,
                                        onStartOutForDelivery = viewModel::startOutForDelivery,
                                        onArrivedAtDoor = viewModel::markArrivedAtDoor,
                                        onAttachPodPhoto = viewModel::attachPodPhoto,
                                        onClearOtpError = viewModel::clearOtpError,
                                        onCompleteDelivery = viewModel::completeDelivery,
                                        onReassignTrip = { orderId, reason ->
                                            viewModel.reassignActiveTrip(orderId, reason)
                                            currentTab = RiderTab.DUTY
                                        },
                                        onGoToDuty = { currentTab = RiderTab.DUTY },
                                        onMessage = viewModel::showMessage
                                    )

                                    RiderTab.EARNINGS -> EarningsScreen(
                                        state = state,
                                        onWithdrawBalance = viewModel::withdrawAvailableBalance,
                                        onSettleCodCash = viewModel::settleCodCash
                                    )

                                    RiderTab.PROFILE -> RiderProfileScreen(
                                        state = state,
                                        onSelectVehicle = viewModel::selectVehicle,
                                        onUpdateChecklist = viewModel::updateChecklist,
                                        onToggleDarkTheme = viewModel::setDarkTheme,
                                        onToggleNotifications = viewModel::setNotificationsEnabled,
                                        onSetScreenZoom = viewModel::setScreenZoomPercent,
                                        onLogout = {
                                            viewModel.logout()
                                            currentTab = RiderTab.DUTY
                                        },
                                        onMessage = viewModel::showMessage
                                    )
                                }
                            }
                        }

                        DeliveryCompleteOverlay(
                            celebration = celebration,
                            onFindNextOrder = {
                                viewModel.dismissCelebration()
                                currentTab = RiderTab.DUTY
                                viewModel.pingNewOrderOffer()
                            },
                            onDismiss = {
                                viewModel.dismissCelebration()
                                currentTab = RiderTab.EARNINGS
                            }
                        )
                    }
                }
            }
        }
    }
}
