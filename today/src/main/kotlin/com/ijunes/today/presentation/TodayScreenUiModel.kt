package com.ijunes.today.presentation

import android.net.Uri
import com.ijunes.mefirst.common.data.Message

/**
 * UI state passed to [TodayScreenProvider.Content].
 *
 * @property messages The ordered list of notes to display in the feed, sourced from
 * [TodayViewModel.conversation]. Each item may represent a text note, a voice recording,
 * or an image attachment.
 * @property pendingImageUri A image URI staged for sending but not yet persisted. Non-null when
 * the user has selected an image from the gallery or camera but has not yet tapped Send.
 */
data class TodayScreenUiModel(
    val messages: List<Message>,
    val pendingImageUri: Uri? = null,
)