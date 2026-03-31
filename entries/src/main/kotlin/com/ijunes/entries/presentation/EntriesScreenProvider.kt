package com.ijunes.entries.presentation

import androidx.compose.runtime.Composable

interface EntriesScreenProvider {
    @Composable
    fun Content(uiModel: EntriesScreenUiModel)
}
