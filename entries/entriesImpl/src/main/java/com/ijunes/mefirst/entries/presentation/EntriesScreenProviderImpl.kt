package com.ijunes.mefirst.entries.presentation

import androidx.compose.runtime.Composable
import com.ijunes.entries.presentation.EntriesScreenProvider
import com.ijunes.entries.presentation.EntriesScreenUiModel

class EntriesScreenProviderImpl : EntriesScreenProvider {
    @Composable
    override fun Content(uiModel: EntriesScreenUiModel) {
        EntriesScreen(uiModel = uiModel)
    }
}
