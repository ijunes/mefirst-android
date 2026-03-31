package com.ijunes.today.presentation

import com.ijunes.mefirst.common.data.MessageItem

/**
 * UI state passed to [TodayScreenProvider.Content].
 *
 * @property messages The ordered list of notes to display in the feed, sourced from
 * [TodayViewModel.conversation]. Each item may represent a text note, a voice recording,
 * or an image attachment.
 */
data class TodayScreenUiModel(
    val messages: List<MessageItem>
)