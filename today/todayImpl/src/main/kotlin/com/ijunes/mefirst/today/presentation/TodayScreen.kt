package com.ijunes.mefirst.today.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.ijunes.mefirst.common.data.Message
import com.ijunes.mefirst.today.presentation.components.ChatBox
import com.ijunes.mefirst.today.presentation.components.DateHeader
import com.ijunes.mefirst.today.presentation.components.MessageItem
import com.ijunes.today.presentation.TodayScreenUiModel

@Preview(showSystemUi = true)
@Composable
fun TodayScreenPreview() {

        TodayScreen(
            uiModel = TodayScreenUiModel(
                messages =
                    listOf(
                        Message(id = "preview", timeStamp = System.currentTimeMillis(), text = "Hello")
                    )
            ),
            isRecording = false,
            onChatSendClickListener = {},
            onGalleryClickListener = {},
            onCameraClickListener = {},
            onClearPendingImageListener = {},
            onDeleteMessage = {},
            contentPadding = PaddingValues(0.dp)
        )

}

/**
 * Today screen content. Displays the day's message feed above a [ChatBox] input area.
 *
 * The feed is pinned to the bottom of the available space and auto-scrolls to the latest item
 * whenever the message count changes. The [ChatBox] is constrained to the bottom of the layout
 * and pushes upward when the soft keyboard appears via [imePadding].
 *
 * @param uiModel UI state for the screen, including the message list and any staged image.
 * @param isRecording Whether a voice recording is currently in progress. Passed to [ChatBox] to
 * show the Stop icon in place of Send/Mic.
 * @param onChatSendClickListener Invoked when the user taps the send button. Receives the current
 * text field value, which may be empty.
 * @param onGalleryClickListener Invoked when the user taps the gallery attachment icon.
 * @param onCameraClickListener Invoked when the user taps the camera icon.
 * @param onClearPendingImageListener Invoked when the user taps the dismiss button on the pending
 * image thumbnail.
 * @param contentPadding Insets from the parent [androidx.compose.material3.Scaffold] consumed via
 * [consumeWindowInsets] so system bars do not overlap content.
 */
@Composable
fun TodayScreen(
    uiModel: TodayScreenUiModel,
    isRecording: Boolean,
    onChatSendClickListener: (String) -> Unit,
    onGalleryClickListener: () -> Unit,
    onCameraClickListener: () -> Unit,
    onClearPendingImageListener: () -> Unit,
    onDeleteMessage: (Message) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {

    ConstraintLayout(
        modifier = modifier
            .fillMaxSize()
            .consumeWindowInsets(contentPadding)
            .imePadding()
    ) {
        val (messages, chatBox) = createRefs()
        val listState = rememberLazyListState()
        LaunchedEffect(uiModel.messages.size) {
            if (uiModel.messages.isNotEmpty()) {
                listState.animateScrollToItem(uiModel.messages.size - 1)
            }
        }

        DateHeader()

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(messages) {
                    top.linkTo(parent.top)
                    bottom.linkTo(chatBox.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    height = Dimension.fillToConstraints
                },
            verticalArrangement = Arrangement.Bottom,
            contentPadding = PaddingValues(16.dp)
        ) {
            items(uiModel.messages, key = { it.id }) { item ->
                MessageItem(message = item) {
                    onDeleteMessage(item)
                }
            }
        }

        ChatBox(
            onSendChatClickListener = onChatSendClickListener,
            isRecording = isRecording,
            pendingImageUri = uiModel.pendingImageUri,
            onGalleryClickListener = onGalleryClickListener,
            onCameraClickListener = onCameraClickListener,
            onClearPendingImageListener = onClearPendingImageListener,
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(chatBox) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )
    }
}

