package com.ijunes.today.domain

import android.net.Uri

sealed interface TodayAction {
    data object RequestRecordPermission : TodayAction
    data object LaunchGallery : TodayAction
    data class LaunchCamera(val uri: Uri) : TodayAction
}