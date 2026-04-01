package com.ijunes.mefirst.main

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.ijunes.mefirst.common.state.OnboardingStateHolder
import com.ijunes.mefirst.common.state.SettingsStateHolder
import com.ijunes.entries.presentation.EntriesScreenProvider
import com.ijunes.entries.presentation.EntriesViewModel
import com.ijunes.mefirst.onboarding.presentation.OnboardingScreen
import com.ijunes.mefirst.settings.pin.PinScreen
import com.ijunes.mefirst.settings.domain.SettingsAction
import com.ijunes.mefirst.settings.presentation.SettingsScreenProvider
import com.ijunes.mefirst.settings.presentation.SettingsViewModel
import com.ijunes.mefirst.ui.theme.AppTheme
import com.ijunes.today.domain.TodayAction
import com.ijunes.today.presentation.TodayScreenProvider
import com.ijunes.today.presentation.TodayViewModel
import kotlinx.coroutines.flow.combine
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val todayVM: TodayViewModel by viewModel()
    private val entriesVM: EntriesViewModel by viewModel()
    private val appModeVM: AppModeViewModel by viewModel()
    private val settingsVM: SettingsViewModel by viewModel()
    private val todayScreenProvider: TodayScreenProvider by inject()
    private val entriesScreenProvider: EntriesScreenProvider by inject()
    private val settingsScreenProvider: SettingsScreenProvider by inject()
    private val onboardingStateHolder: OnboardingStateHolder by inject()
    private val settingsStateHolder: SettingsStateHolder by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedImageUri: Uri? = if (intent?.action == Intent.ACTION_SEND) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM)
        } else null

        val sharedText: String? = if (intent?.action == Intent.ACTION_SEND &&
            intent.type == "text/plain") {
            intent.getStringExtra(Intent.EXTRA_TEXT)
        } else null


        enableEdgeToEdge()
        setContent {
            var showOnboarding by remember { mutableStateOf(!onboardingStateHolder.isOnboardingComplete) }
            var isPinVerified by remember { mutableStateOf(settingsStateHolder.pinHash == null) }

            if (showOnboarding) {
                AppTheme {
                    OnboardingScreen(
                        onComplete = {
                            onboardingStateHolder.markComplete()
                            showOnboarding = false
                        }
                    )
                }
                return@setContent
            }

            if (!isPinVerified) {
                AppTheme {
                    PinScreen(
                        onVerify = { pin ->
                            val correct = settingsStateHolder.verifyPin(pin)
                            if (correct) isPinVerified = true
                            correct
                        }
                    )
                }
                return@setContent
            }

            val navHostController = rememberNavController()

            val uiState by remember {
                combine(
                    todayVM.conversation,
                    entriesVM.entries,
                    todayVM.isRecording,
                    appModeVM.isWorkMode,
                    todayVM.pendingImageUri,
                ) { conversation, entries, isRecording, isWorkMode, pendingImageUri ->
                    MainScreenUiState(conversation, entries, isRecording, isWorkMode, pendingImageUri)
                }
            }.collectAsState(initial = MainScreenUiState())

            val requestPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) todayVM.startRecording()
            }

            val galleryLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.GetContent()
            ) { uri ->
                if (uri != null) todayVM.setPendingImage(uri)
            }

            var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
            val cameraLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.TakePicture()
            ) { success ->
                if (success) cameraImageUri?.let { todayVM.setPendingImage(it) }
                cameraImageUri = null
            }

            val backupLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.CreateDocument("application/zip")
            ) { uri ->
                if (uri != null) settingsVM.performBackup(uri)
            }

            val restoreLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.GetContent()
            ) { uri ->
                if (uri != null) settingsVM.performRestore(uri)
            }

            LaunchedEffect(Unit) {
                todayVM.actions.collect { command ->
                    when (command) {
                        TodayAction.RequestRecordPermission ->
                            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        TodayAction.LaunchGallery ->
                            galleryLauncher.launch("image/*")
                        is TodayAction.LaunchCamera -> {
                            cameraImageUri = command.uri
                            cameraLauncher.launch(command.uri)
                        }
                    }
                }
            }

            LaunchedEffect(Unit) {
                settingsVM.actions.collect { command ->
                    when (command) {
                        SettingsAction.LaunchBackupPicker ->
                            backupLauncher.launch("mefirst_backup.zip")
                        SettingsAction.LaunchRestorePicker ->
                            restoreLauncher.launch("application/zip")
                    }
                }
            }

            LaunchedEffect(sharedImageUri) {
                sharedImageUri?.let { todayVM.setPendingImage(it) }
            }

            LaunchedEffect(sharedText) {
                sharedText?.let { todayVM.insertNote(it) }
            }

            AppTheme(isWorkMode = uiState.isWorkMode) {
                MainScreen(
                    navController = navHostController,
                    uiState = uiState,
                    onEvent = todayVM::handleEvent,
                    todayScreenProvider = todayScreenProvider,
                    entriesScreenProvider = entriesScreenProvider,
                    settingsScreenProvider = settingsScreenProvider,
                    settingsViewModel = settingsVM,
                )
            }
        }
    }
}
