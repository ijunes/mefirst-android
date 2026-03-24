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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ijunes.mefirst.entries.presentation.EntriesScreen
import com.ijunes.mefirst.entries.presentation.EntriesScreenUiModel
import com.ijunes.mefirst.settings.presentation.SettingsScreen
import com.ijunes.mefirst.main.components.ModeToggleAction
import com.ijunes.mefirst.main.nav.MainScreenNavRoutes
import com.ijunes.mefirst.main.nav.Route
import com.ijunes.mefirst.main.nav.toNavItem
import com.ijunes.mefirst.today.presentation.TodayScreen
import com.ijunes.mefirst.today.presentation.TodayScreenUiModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    uiState: MainScreenUiState,
    onEvent: (MainAction) -> Unit,
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ModeToggleAction(uiState.isWorkMode) { onEvent(MainAction.SetWorkMode(it)) }
                    }
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
                TodayScreen(
                    uiModel = TodayScreenUiModel(messages = uiState.conversation),
                    isRecording = uiState.isRecording,
                    onChatSendClickListener = { onEvent(MainAction.SendChat(it)) },
                    onGalleryClickListener = { onEvent(MainAction.OpenGallery) },
                    onCameraClickListener = { onEvent(MainAction.OpenCamera) },
                    contentPadding = contentPadding
                )
            }
            composable(MainScreenNavRoutes.Entries.route.name) {
                EntriesScreen(uiModel = EntriesScreenUiModel(entries = uiState.entries))
            }
            composable(MainScreenNavRoutes.Settings.route.name) {
                SettingsScreen()
            }
        }
    }
}
