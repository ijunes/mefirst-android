package com.ijunes.mefirst.today.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ijunes.mefirst.common.components.VoiceNotePlayer
import com.ijunes.mefirst.common.data.Message
import com.ijunes.mefirst.common.util.toTimeString
import com.ijunes.mefirst.database.model.MediaType

@Composable
fun MessageItem(
    modifier: Modifier = Modifier,
    message: Message
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
                .background(MaterialTheme.colorScheme.onTertiaryFixed)
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
