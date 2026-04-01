package com.ijunes.mefirst.settings.presentation

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.ijunes.mefirst.settings.domain.SettingsAction
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Abstract ViewModel for the Settings feature.
 *
 * Defined in the `:settings` API module so that `:app` can interact with the Settings feature
 * without a direct dependency on `:settings:settingsImpl`. The concrete implementation
 * ([com.ijunes.mefirst.settings.presentation.SettingsViewModelImpl]) lives in
 * `:settings:settingsImpl` and is bound to this class via Koin in `:settings:settingsApp`.
 */
abstract class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    /** The current UI state for the Settings screen. */
    abstract val uiState: StateFlow<SettingsUiState>

    /**
     * One-shot commands for the host Activity that require Activity-level APIs such as file
     * picker launchers. Collect this in the Activity and dispatch each [SettingsAction] to the
     * appropriate launcher.
     */
    abstract val actions: SharedFlow<SettingsAction>

    /** Updates the daily flush time and reschedules the alarm. */
    abstract fun setFlushTime(hour: Int, minute: Int)

    /** Emits [SettingsAction.LaunchBackupPicker] so the Activity can open a file-save dialog. */
    abstract fun startBackup()

    /** Writes a backup ZIP to the given [uri]. Called by the Activity after the picker returns. */
    abstract fun performBackup(uri: Uri)

    /** Emits [SettingsAction.LaunchRestorePicker] so the Activity can open a file-open dialog. */
    abstract fun startRestore()

    /** Restores data from the given backup [uri]. Called by the Activity after the picker returns. */
    abstract fun performRestore(uri: Uri)

    /** Stores a new PIN hash. */
    abstract fun setPin(pin: String)

    /**
     * Removes the PIN after verifying [currentPin].
     * @return `true` if [currentPin] matched the stored PIN and the PIN was removed.
     */
    abstract fun removePin(currentPin: String): Boolean

    /** @return `true` if [pin] matches the stored PIN hash. */
    abstract fun verifyPin(pin: String): Boolean

    /** Clears the [SettingsUiState.backupResult] after the result dialog is dismissed. */
    abstract fun dismissBackupResult()
}
