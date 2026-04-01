package com.ijunes.mefirst.main

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ijunes.mefirst.common.action.MainAction
import com.ijunes.entries.presentation.EntriesScreenProvider
import com.ijunes.entries.presentation.EntriesScreenUiModel
import com.ijunes.mefirst.settings.presentation.SettingsScreenProvider
import com.ijunes.mefirst.settings.presentation.SettingsViewModel
import com.ijunes.mefirst.main.components.SegmentedModeSelector
import com.ijunes.mefirst.main.nav.MainScreenNavRoutes
import com.ijunes.mefirst.main.nav.Route
import com.ijunes.mefirst.main.nav.toNavItem
import com.ijunes.today.presentation.TodayScreenProvider
import com.ijunes.today.presentation.TodayScreenUiModel

/**
 * Root composable for the main app shell.
 *
 * Renders a [Scaffold] with a top app bar, a bottom navigation bar, and a [NavHost] that
 * switches between the Today, Entries, and Settings destinations. Screen content is delegated
 * to provider interfaces ([TodayScreenProvider], [EntriesScreenProvider],
 * [SettingsScreenProvider]) so that `:app` has no direct dependency on any feature
 * implementation module.
 *
 * Navigation uses `launchSingleTop = true` and `restoreState = true` so that tapping a
 * bottom-bar item that is already selected does not create a duplicate back-stack entry and
 * preserves scroll/input state when switching between tabs.
 *
 * @param navController The [NavHostController] that drives tab navigation.
 * @param uiState Aggregated UI state produced by [MainActivity] from the active ViewModels.
 * @param onEvent Dispatcher for [MainAction] events raised by child screens.
 * @param todayScreenProvider Composable provider for the Today destination.
 * @param entriesScreenProvider Composable provider for the Entries destination.
 * @param settingsScreenProvider Composable provider for the Settings destination.
 * @param settingsViewModel Passed directly to the Settings screen because its provider
 * interface requires a typed reference rather than a generic event callback.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    uiState: MainScreenUiState,
    onEvent: (MainAction) -> Unit,
    todayScreenProvider: TodayScreenProvider,
    entriesScreenProvider: EntriesScreenProvider,
    settingsScreenProvider: SettingsScreenProvider,
    settingsViewModel: SettingsViewModel,
) {
    val navItems = Route.entries.map {
        it.toNavItem(LocalContext.current)
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("MeFirst")
                },
                actions = {
                    SegmentedModeSelector(uiState.isWorkMode) { onEvent(MainAction.SetWorkMode(it)) }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                navItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }) { contentPadding ->
        NavHost(
            navController = navController,
            startDestination = "today",
            modifier = Modifier.padding(contentPadding)
        ) {
            composable(MainScreenNavRoutes.Today.route.name) {
                todayScreenProvider.Content(
                    uiModel = TodayScreenUiModel(
                        messages = uiState.conversation,
                        pendingImageUri = uiState.pendingImageUri,
                    ),
                    isRecording = uiState.isRecording,
                    onChatSendClickListener = { onEvent(MainAction.SendChat(it)) },
                    onGalleryClickListener = { onEvent(MainAction.OpenGallery) },
                    onCameraClickListener = { onEvent(MainAction.OpenCamera) },
                    onClearPendingImageListener = { onEvent(MainAction.ClearPendingImage) },
                    contentPadding = contentPadding,
                )
            }
            composable(MainScreenNavRoutes.Entries.route.name) {
                entriesScreenProvider.Content(uiModel = EntriesScreenUiModel(entries = uiState.entries))
            }
            composable(MainScreenNavRoutes.Settings.route.name) {
                settingsScreenProvider.Content(viewModel = settingsViewModel)
            }
        }
    }
}
