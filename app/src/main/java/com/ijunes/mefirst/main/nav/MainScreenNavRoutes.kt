package com.ijunes.mefirst.main.nav

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.ijunes.mefirst.R

sealed class MainScreenNavRoutes(
    @param:StringRes val labelResId: Int,
    val icon: ImageVector,
    val route: Route
) {
    object Today : MainScreenNavRoutes(R.string.nav_today_label, Icons.Default.EditNote, Route.TODAY)
    object Entries : MainScreenNavRoutes(R.string.nav_entries_label, Icons.Default.Book, Route.ENTRIES)
    object Settings : MainScreenNavRoutes(R.string.nav_settings_label, Icons.Default.Settings, Route.SETTINGS)
}

enum class Route {
    TODAY, ENTRIES, SETTINGS
}

fun Route.toNavItem(context: Context): NavItem{
    val navRoute =  when (this) {
        Route.TODAY -> MainScreenNavRoutes.Today
        Route.ENTRIES -> MainScreenNavRoutes.Entries
        Route.SETTINGS -> MainScreenNavRoutes.Settings
    }
    return NavItem(label = context.getString(navRoute.labelResId), icon = navRoute.icon, route = navRoute.route.name)
}