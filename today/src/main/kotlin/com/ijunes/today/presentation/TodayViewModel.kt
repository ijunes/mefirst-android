package com.ijunes.today.presentation

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.ijunes.mefirst.common.action.MainAction
import com.ijunes.mefirst.common.data.Message
import com.ijunes.today.domain.TodayAction
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow


/**
 * Abstract ViewModel for the Today feature.
 *
 * Defined in the `:today` API module so that `:app` can depend on the Today feature without a
 * direct compile-time dependency on `:today:todayImpl`. The concrete implementation
 * ([com.ijunes.mefirst.today.presentation.TodayScreenViewModelImpl]) lives in `:today:todayImpl`
 * and is bound to this class via Koin in `:today:todayApp`.
 *
 * ## Interaction model
 * All UI-driven events flow in through [handleEvent]. Actions that require Activity-level APIs
 * (permission requests, intent launchers) are emitted as one-shot [TodayAction] commands on
 * [actions] and handled by the host Activity. The Activity feeds results back through
 * [setPendingImage] and [startRecording] rather than through [handleEvent], keeping the
 * ViewModel free of Activity references.
 *
 * ## Pending-image flow
 * Selecting an image (gallery, camera, or share intent) calls [setPendingImage], which stores
 * the URI in [pendingImageUri] without writing to the database. The image is only persisted when
 * [handleEvent] receives [MainAction.SendChat] while [pendingImageUri] is non-null. The user can
 * discard the staged image at any time via [MainAction.ClearPendingImage].
 */
abstract class TodayViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * Ordered list of notes for the active mode (personal or work), backed by the database and
     * emitted whenever the underlying data changes. Switches its upstream source automatically
     * when the user toggles work mode.
     */
    abstract val conversation: StateFlow<List<Message>>

    /** `true` while a voice recording is in progress. */
    abstract val isRecording: StateFlow<Boolean>

    /**
     * One-shot commands for the host Activity that require Activity-level APIs such as permission
     * launchers or intent launchers. The Activity collects this flow and dispatches each
     * [TodayAction] to the appropriate launcher.
     */
    abstract val actions: SharedFlow<TodayAction>

    /**
     * Image URI staged for sending but not yet persisted. Non-null after the user selects an
     * image via the gallery, camera, or a share intent. Cleared when [handleEvent] receives
     * [MainAction.SendChat] (image is committed to the database) or [MainAction.ClearPendingImage]
     * (image is discarded).
     */
    abstract val pendingImageUri: StateFlow<Uri?>

    /**
     * Entry point for all user interactions from the Today screen or its toolbar.
     *
     * Key dispatch rules:
     * - [MainAction.SendChat] with non-empty text → persists a text note.
     * - [MainAction.SendChat] with empty text and a pending image → commits [pendingImageUri].
     * - [MainAction.SendChat] with empty text and no pending image → toggles voice recording
     *   (requests [android.Manifest.permission.RECORD_AUDIO] if not yet granted).
     * - [MainAction.ClearPendingImage] → discards [pendingImageUri].
     * - [MainAction.DeleteToday] → clears all of today's notes.
     * - [MainAction.OpenGallery] / [MainAction.OpenCamera] → emits the corresponding
     *   [TodayAction] on [actions] for the Activity to handle.
     * - [MainAction.SetWorkMode] → switches the active data source and theme.
     */
    abstract fun handleEvent(event: MainAction)

    /**
     * Begins capturing audio from the microphone. Called by the Activity after
     * [android.Manifest.permission.RECORD_AUDIO] has been granted in response to a
     * [TodayAction.RequestRecordPermission] command.
     */
    abstract fun startRecording()

    /**
     * Stages [uri] as the pending image without persisting it. Called by the Activity after
     * the gallery or camera launcher returns a result, or when the app receives a share intent
     * with an image. The URI is stored in [pendingImageUri] and committed to the database only
     * when the user triggers [MainAction.SendChat].
     *
     * @param uri Content or file [Uri] of the image to attach.
     */
    abstract fun setPendingImage(uri: Uri)

    /**
     * Persists a plain-text note immediately.
     *
     * @param msg The text content to save.
     */
    abstract fun insertNote(msg: String)
}
