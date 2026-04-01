package com.ijunes.mefirst.common.action

import com.ijunes.mefirst.common.data.Message

/**
 * User-initiated actions that originate from the Today screen and are dispatched to
 * [com.ijunes.today.presentation.TodayViewModel.handleEvent].
 *
 * Actions that require Activity-level APIs (permissions, intent launchers) are translated into
 * [com.ijunes.today.domain.TodayAction] commands by the ViewModel and collected by the host
 * Activity.
 */
sealed interface MainAction {

    /**
     * The user tapped the send button.
     *
     * @property text The current text field value. An empty string signals a voice recording
     * toggle (start or stop), unless a pending image is staged — in which case the image is
     * committed instead.
     */
    data class SendChat(val text: String) : MainAction

    /** The user requested that today's notes be deleted. */
    data object DeleteToday : MainAction

    /** The user tapped the gallery attachment button to pick an image from their photo library. */
    data object OpenGallery : MainAction

    /** The user tapped the camera button to capture a new photo. */
    data object OpenCamera : MainAction

    /**
     * The user dismissed the pending image preview without sending it.
     * Clears [com.ijunes.today.presentation.TodayViewModel.pendingImageUri].
     */
    data object ClearPendingImage : MainAction

    /**
     * The user toggled between personal and work mode.
     *
     * @property isWork `true` to switch to work mode, `false` for personal mode.
     */
    data class SetWorkMode(val isWork: Boolean) : MainAction

    /**
     * The user tapped the delete button on a message.
     *
     * @property message The message to delete.
     */
    data class DeleteMessage(val message: Message) : MainAction
}