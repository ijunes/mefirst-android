package com.ijunes.mefirst.settings.presentation

data class SettingsUiState(
    val flushHour: Int = 0,
    val flushMinute: Int = 0,
    val hasPin: Boolean = false,
    val isBackingUp: Boolean = false,
    val isRestoring: Boolean = false,
    val backupResult: BackupResult? = null,
)

sealed interface BackupResult {
    data object Success : BackupResult
    data class Error(val message: String) : BackupResult
}
