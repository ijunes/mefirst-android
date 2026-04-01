package com.ijunes.mefirst.today.presentation

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.compose.AsyncImage
import com.ijunes.mefirst.common.data.Message
import com.ijunes.mefirst.common.util.toDateString
import com.ijunes.mefirst.today.impl.R
import com.ijunes.mefirst.today.presentation.components.MessageItem
import com.ijunes.today.presentation.TodayScreenUiModel


@Preview(showSystemUi = true)
@Composable
fun TodayScreenPreview() {

        TodayScreen(
            uiModel = TodayScreenUiModel(
                messages =
                    listOf(
                        Message(timeStamp = System.currentTimeMillis(), text = "Hello")
                    )
            ),
            isRecording = false,
            onChatSendClickListener = {},
            onGalleryClickListener = {},
            onCameraClickListener = {},
            onClearPendingImageListener = {},
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
            items(uiModel.messages) { item ->
                MessageItem(message = item)
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



/**
 * Chat input bar composed of an optional pending-image preview, a [ChatTextField], and a
 * send/mic/stop action button.
 *
 * **Send button icon logic:**
 * - Stop icon — while [isRecording] is `true`.
 * - Send icon — when [pendingImageUri] is non-null, or when the text field is non-empty.
 * - Mic icon — when the text field is empty and no image is staged (tapping starts/requests
 *   a voice recording).
 *
 * When the text field is non-empty and the user taps Send, the field is cleared after the
 * callback fires. For pending images the ViewModel is responsible for clearing [pendingImageUri]
 * after it commits the note.
 *
 * @param onSendChatClickListener Invoked with the current text field value when Send is tapped.
 * @param isRecording Whether a voice recording is active.
 * @param pendingImageUri A staged image URI to preview. When non-null, a 72dp thumbnail with a
 * dismiss button is shown above the text row.
 * @param onGalleryClickListener Invoked when the gallery icon inside [ChatTextField] is tapped.
 * @param onCameraClickListener Invoked when the camera icon inside [ChatTextField] is tapped.
 * @param onClearPendingImageListener Invoked when the user taps the dismiss button on the
 * pending image thumbnail.
 */
@Composable
fun ChatBox(
    onSendChatClickListener: (String) -> Unit,
    isRecording: Boolean,
    pendingImageUri: Uri?,
    onGalleryClickListener: () -> Unit,
    onCameraClickListener: () -> Unit,
    onClearPendingImageListener: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Hold MutableState explicitly (not `by` delegate) so the lambdas below capture
    // the stable state *object*, not its value; preventing IconButton recompositions
    // on every keystroke.
    val chatBoxValue = remember { mutableStateOf(TextFieldValue("")) }

    Column(modifier = modifier.fillMaxWidth()) {
        if (pendingImageUri != null) {
            Box(
                modifier = Modifier
                    .padding(start = 12.dp, top = 8.dp)
            ) {
                AsyncImage(
                    model = pendingImageUri,
                    contentDescription = "Pending image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                IconButton(
                    onClick = onClearPendingImageListener,
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove image",
                        modifier = Modifier.padding(2.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // State is read inside ChatTextField only, isolating recompositions there.
            ChatTextField(
                state = chatBoxValue,
                onGalleryClickListener = onGalleryClickListener,
                onCameraClickListener = onCameraClickListener,
                modifier = Modifier.weight(1.0f)
            )
            IconButton(
                onClick = {
                    val text = chatBoxValue.value.text
                    onSendChatClickListener(text)
                    if (text.isNotEmpty()) chatBoxValue.value = TextFieldValue("")
                },
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .align(Alignment.CenterVertically)
            ) {
                val isEmpty = chatBoxValue.value.text.isEmpty()
                val (icon, description) = when {
                    isRecording -> Icons.Default.Stop to "Stop recording"
                    isEmpty && pendingImageUri == null -> Icons.Default.Mic to "Record voice"
                    else -> Icons.AutoMirrored.Default.Send to "Send"
                }
                Icon(
                    imageVector = icon,
                    contentDescription = description,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )
            }
        }
    }
}

/**
 * Pill-shaped text field with gallery and camera attachment icons in the trailing slot.
 *
 * State is accepted as a [MutableState] object rather than a plain value so that the gallery
 * and camera [IconButton] lambdas capture the stable wrapper instead of a snapshot value,
 * preventing unnecessary recompositions of those buttons on every keystroke.
 *
 * @param state Mutable holder for the current [TextFieldValue].
 * @param onGalleryClickListener Invoked when the gallery icon is tapped.
 * @param onCameraClickListener Invoked when the camera icon is tapped.
 */
@Composable
private fun ChatTextField(
    state: MutableState<TextFieldValue>,
    onGalleryClickListener: () -> Unit,
    onCameraClickListener: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = state.value,
        onValueChange = { state.value = it },
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        placeholder = { Text(stringResource(id = R.string.today_personal_chat_placeholder)) },
        trailingIcon = {
            Row {
                IconButton(onClick = onGalleryClickListener) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Attach image from gallery"
                    )
                }
                IconButton(onClick = onCameraClickListener) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Take photo with camera"
                    )
                }
            }
        }
    )
}

/** Displays today's date centred at the top of the feed. */
@Composable
fun DateHeader() {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        textAlign = TextAlign.Center,
        fontWeight = Bold,
        color = MaterialTheme.colorScheme.secondary,
        text = "- " + System.currentTimeMillis().toDateString() + " -"
    )
}
