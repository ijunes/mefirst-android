package com.ijunes.mefirst.settings.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

class SettingsScreenProviderImpl : SettingsScreenProvider {

    @Composable
    override fun Content(
        viewModel: SettingsViewModel,
        modifier: Modifier,
        contentPadding: PaddingValues,
    ) {
        SettingsScreen(viewModel = viewModel, modifier = modifier)
    }
}
