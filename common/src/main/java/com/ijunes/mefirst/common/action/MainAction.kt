package com.ijunes.mefirst.common.action

sealed interface MainAction {
    data class SendChat(val text: String) : MainAction
    data object DeleteToday : MainAction
    data object OpenGallery : MainAction
    data object OpenCamera : MainAction
    data class SetWorkMode(val isWork: Boolean) : MainAction
}