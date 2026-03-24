package com.ijunes.mefirst.today.presentation

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
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
import com.ijunes.mefirst.R
import com.ijunes.mefirst.common.data.model.MediaType
import com.ijunes.mefirst.common.data.MessageItem
import com.ijunes.mefirst.common.components.VoiceNotePlayer
import com.ijunes.mefirst.ui.theme.AppTheme
import com.ijunes.mefirst.common.util.toDateString
import com.ijunes.mefirst.common.util.toTimeString


@Preview(showSystemUi = true)
@Composable
fun TodayScreenPreview() {
    AppTheme {
        TodayScreen(
            uiModel = TodayScreenUiModel(
                messages =
                    listOf(
                        MessageItem(timeStamp = System.currentTimeMillis(), text = "Hello")
                    )
            ),
            isRecording = false,
            onChatSendClickListener = {},
            onGalleryClickListener = {},
            onCameraClickListener = {},
            contentPadding = PaddingValues(0.dp)
        )
    }
}

@Composable
fun TodayScreen(
    uiModel: TodayScreenUiModel,
    isRecording: Boolean,
    onChatSendClickListener: (String) -> Unit,
    onGalleryClickListener: () -> Unit,
    onCameraClickListener: () -> Unit,
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
            onGalleryClickListener = onGalleryClickListener,
            onCameraClickListener = onCameraClickListener,
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

@Composable
fun MessageItem(
    modifier: Modifier = Modifier,
    message: MessageItem
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.End)
                .clip(
                    RoundedCornerShape(
                        topStart = 48f,
                        topEnd = 48f,
                        bottomStart = 48f,
                        bottomEnd = 0f
                    )
                )
                .background(MaterialTheme.colorScheme.onBackground)
                .padding(16.dp)
        ) {
            when (message.mediaType) {
                MediaType.VOICE -> VoiceNotePlayer(uri = message.mediaPath, waveformUri = message.waveformPath)
                MediaType.IMAGE -> AsyncImage(
                    model = message.mediaPath,
                    contentDescription = "Image attachment",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .fillMaxSize(0.5f)
                )
                else -> message.text?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.primaryContainer
                    )
                }
            }
        }
        Text(
            text = message.timeStamp.toTimeString(),
            fontSize = MaterialTheme.typography.bodySmall.fontSize,
            modifier = Modifier.align(Alignment.End))
    }
}



@Composable
fun ChatBox(
    onSendChatClickListener: (String) -> Unit,
    isRecording: Boolean,
    onGalleryClickListener: () -> Unit,
    onCameraClickListener: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Hold MutableState explicitly (not `by` delegate) so the lambdas below capture
    // the stable state *object*, not its value; preventing IconButton recompositions
    // on every keystroke.
    val chatBoxValue = remember { mutableStateOf(TextFieldValue("")) }

    Row(
        modifier = modifier
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
                isEmpty -> Icons.Default.Mic to "Record voice"
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
