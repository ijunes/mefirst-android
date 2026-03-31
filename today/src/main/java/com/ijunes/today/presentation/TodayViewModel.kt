package com.ijunes.today.presentation

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.ijunes.mefirst.common.action.MainAction
import com.ijunes.mefirst.common.data.MessageItem
import com.ijunes.today.domain.TodayAction
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Abstract ViewModel for the Today feature.
 *
 * Defined in the `:today` API module so that `:app` can interact with the Today feature without
 * a direct dependency on `:today:todayImpl`. The concrete implementation
 * ([com.ijunes.mefirst.today.presentation.TodayScreenViewModelImpl]) lives in `:today:todayImpl`
 * and is bound to this class via Koin in `:today:todayApp`.
 */
abstract class TodayViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * The ordered list of notes for the active mode (personal or work), emitted whenever the
     * underlying data changes. Switches its source automatically when the user toggles work mode.
     */
    abstract val conversation: StateFlow<List<MessageItem>>

    /** Whether a voice recording is currently in progress. */
    abstract val isRecording: StateFlow<Boolean>

    /**
     * One-shot commands for the host Activity that require Activity-level APIs such as permission
     * launchers or intent launchers. Collect this in the Activity and dispatch each [TodayAction]
     * to the appropriate launcher.
     */
    abstract val activityCommands: SharedFlow<TodayAction>

    /**
     * Entry point for all user interactions originating from the Today screen or its toolbar.
     *
     * @param event A [MainAction] describing the user's intent (send a message, open the gallery,
     * open the camera, toggle work mode, or delete today's notes).
     */
    abstract fun handleEvent(event: MainAction)

    /**
     * Begins capturing audio from the microphone.
     * Called by the Activity after the [android.Manifest.permission.RECORD_AUDIO] permission has
     * been granted in response to a [TodayAction.RequestRecordPermission] command.
     */
    abstract fun startRecording()

    /**
     * Persists an image note from the given [uri].
     * Called by the Activity after the camera or gallery launcher returns a result.
     *
     * @param uri The content or file [Uri] of the image to attach.
     */
    abstract fun insertImageNote(uri: Uri)
}
