package com.ijunes.mefirst.today.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.ijunes.today.presentation.TodayScreenProvider
import com.ijunes.today.presentation.TodayScreenUiModel

class TodayScreenProviderImpl : TodayScreenProvider {
    @Composable
    override fun Content(
        uiModel: TodayScreenUiModel,
        isRecording: Boolean,
        onChatSendClickListener: (String) -> Unit,
        onGalleryClickListener: () -> Unit,
        onCameraClickListener: () -> Unit,
        onClearPendingImageListener: () -> Unit,
        contentPadding: PaddingValues,
    ) {
        TodayScreen(
            uiModel = uiModel,
            isRecording = isRecording,
            onChatSendClickListener = onChatSendClickListener,
            onGalleryClickListener = onGalleryClickListener,
            onCameraClickListener = onCameraClickListener,
            onClearPendingImageListener = onClearPendingImageListener,
            contentPadding = contentPadding,
        )
    }
}
