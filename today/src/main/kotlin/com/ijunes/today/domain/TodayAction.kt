package com.ijunes.today.domain

import android.net.Uri

/**
 * One-shot commands emitted by [com.ijunes.today.presentation.TodayViewModel] to the host
 * Activity.
 *
 * These actions require Activity-level APIs (permission launchers, intent launchers) that cannot
 * be invoked directly from a Composable or ViewModel. The Activity collects
 * [com.ijunes.today.presentation.TodayViewModel.actions] and dispatches each action to
 * the appropriate launcher.
 */
sealed interface TodayAction {

    /** Requests the [android.Manifest.permission.RECORD_AUDIO] runtime permission. */
    data object RequestRecordPermission : TodayAction

    /** Opens the system image picker so the user can attach a photo from their gallery. */
    data object LaunchGallery : TodayAction

    /**
     * Opens the camera to capture a new photo.
     *
     * @property uri A [Uri] pre-created via [androidx.core.content.FileProvider] that the camera
     * app will write the captured image to. The same [Uri] must be passed back to
     * [com.ijunes.today.presentation.TodayViewModel.insertImageNote] on success.
     */
    data class LaunchCamera(val uri: Uri) : TodayAction
}