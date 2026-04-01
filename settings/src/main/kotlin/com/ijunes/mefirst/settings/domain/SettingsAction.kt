package com.ijunes.mefirst.settings.domain

sealed interface SettingsAction {
    data object LaunchBackupPicker : SettingsAction
    data object LaunchRestorePicker : SettingsAction
    data object RestartApp : SettingsAction
}