package com.ijunes.today.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable

interface TodayScreenProvider {
    @Composable
    fun Content(
        uiModel: TodayScreenUiModel,
        isRecording: Boolean,
        onChatSendClickListener: (String) -> Unit,
        onGalleryClickListener: () -> Unit,
        onCameraClickListener: () -> Unit,
        contentPadding: PaddingValues,
    )
}
