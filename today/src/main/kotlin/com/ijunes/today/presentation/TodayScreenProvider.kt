package com.ijunes.today.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable

/**
 * Composable contract for rendering the Today screen.
 *
 * Defined in the `:today` API module so that `:app` can display the Today screen without a direct
 * dependency on `:today:todayImpl`. The concrete implementation ([com.ijunes.mefirst.today.presentation.TodayScreenProviderImpl])
 * lives in `:today:todayImpl` and is bound to this interface via Koin in `:today:todayApp`.
 */
interface TodayScreenProvider {

    /**
     * Renders the Today screen.
     *
     * @param uiModel The current UI state containing the list of messages to display.
     * @param isRecording Whether a voice recording is currently in progress; controls the
     * send/stop button state.
     * @param onChatSendClickListener Invoked when the user taps send. Receives the current text
     * field value, which may be empty (indicating a voice recording toggle).
     * @param onGalleryClickListener Invoked when the user taps the gallery attachment button.
     * @param onCameraClickListener Invoked when the user taps the camera attachment button.
     * @param contentPadding Insets from the parent [androidx.compose.material3.Scaffold] that
     * the screen must respect to avoid being obscured by system bars.
     */
    @Composable
    fun Content(
        uiModel: TodayScreenUiModel,
        isRecording: Boolean,
        onChatSendClickListener: (String) -> Unit,
        onGalleryClickListener: () -> Unit,
        onCameraClickListener: () -> Unit,
        onClearPendingImageListener: () -> Unit,
        contentPadding: PaddingValues,
    )
}
