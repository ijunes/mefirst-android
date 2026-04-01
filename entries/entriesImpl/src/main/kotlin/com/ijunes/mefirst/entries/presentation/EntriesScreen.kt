package com.ijunes.mefirst.entries.presentation

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ijunes.mefirst.database.model.MediaType
import com.ijunes.mefirst.common.data.Message
import com.ijunes.mefirst.common.components.VoiceNotePlayer
import com.ijunes.mefirst.ui.theme.AppTheme
import com.ijunes.mefirst.common.util.toDateString
import com.ijunes.mefirst.common.util.toTimeString
import com.ijunes.entries.presentation.EntriesScreenUiModel

@Preview
@Composable
fun EntriesScreenPreview() {
    AppTheme {
        EntriesScreen(
            uiModel = EntriesScreenUiModel(
                entries = mapOf(
                    100000L to listOf(
                        Message(System.currentTimeMillis(), "Hello"),
                        Message(System.currentTimeMillis() + 10000, "Hello"),
                        Message(System.currentTimeMillis() + 20000, "Hello"),
                    ),
                    300000L to listOf(
                        Message(System.currentTimeMillis(), "Hello"),
                        Message(System.currentTimeMillis() + 10000, "Hello"),
                        Message(System.currentTimeMillis() + 20000, "Hello"),
                    )
                )
            )
        )
    }
}

@Composable
fun EntriesScreen(
    uiModel: EntriesScreenUiModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = spacedBy(8.dp)
    ) {
        items(uiModel.entries.entries.toList(), key = { it.key }) { (date, messages) ->
            EntryComposable(messages.toEntryUiState(date))
        }
    }
}

private fun List<Message>.toEntryUiState(date: Long): EntryUiState {
    return EntryUiState(date.toDateString(), messages = this)
}

@Composable
fun EntryComposable(
    entryUiState: EntryUiState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text(
            modifier = Modifier.padding(8.dp),
            text = entryUiState.date,
            fontWeight = FontWeight.Bold,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            entryUiState.messages.forEach {
                MessageComposable(it)
            }
        }
    }
}

@Composable
fun MessageComposable(item: Message) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = item.timeStamp.toTimeString(),
            fontSize = MaterialTheme.typography.bodySmall.fontSize,
            color = MaterialTheme.colorScheme.secondary
        )
        when (item.mediaType) {
            MediaType.VOICE -> VoiceNotePlayer(uri = item.mediaPath, waveformUri = item.waveformPath)
            MediaType.IMAGE -> AsyncImage(
                model = item.mediaPath,
                contentDescription = "Image attachment",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .fillMaxSize(0.5f)
            )
            else -> item.text?.let { Text(text = it) }
        }
        Spacer(Modifier.height(8.dp))
    }
}

data class EntryUiState(
    val date: String,
    @Stable val messages: List<Message>
)
