package com.ijunes.mefirst.settings.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Composable contract for rendering the Settings screen.
 *
 * Defined in the `:settings` API module so that `:app` can display the Settings screen without
 * a direct dependency on `:settings:settingsImpl`. The concrete implementation
 * ([com.ijunes.mefirst.settings.presentation.SettingsScreenProviderImpl]) lives in
 * `:settings:settingsImpl` and is bound to this interface via Koin in `:settings:settingsApp`.
 */
interface SettingsScreenProvider {

    /**
     * Renders the Settings screen.
     *
     * @param viewModel The [SettingsViewModel] instance scoped to the host Activity.
     * @param modifier Optional [Modifier] applied to the root of the screen.
     * @param contentPadding Insets from the parent [androidx.compose.material3.Scaffold] that
     * the screen must respect to avoid being obscured by system bars.
     */
    @Composable
    fun Content(
        viewModel: SettingsViewModel,
        modifier: Modifier = Modifier,
        contentPadding: PaddingValues = PaddingValues(),
    )
}
