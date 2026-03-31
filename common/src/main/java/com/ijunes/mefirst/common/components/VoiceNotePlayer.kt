package com.ijunes.mefirst.common.components

import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ijunes.mefirst.R
import kotlinx.coroutines.delay

@Composable
fun VoiceNotePlayer(uri: Uri?, waveformUri: Uri? = null) {
    if (uri == null) return
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    val player = remember {
        MediaPlayer().apply {
            setDataSource(context, uri)
            prepare()
        }
    }
    val duration = remember { player.duration }

    DisposableEffect(uri) {
        player.setOnCompletionListener {
            isPlaying = false
            progress = 0f
        }
        onDispose { player.release() }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            if (duration > 0) {
                progress = player.currentPosition.toFloat() / duration
            }
            delay(100)
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = {
            if (isPlaying) player.pause() else player.start()
            isPlaying = !isPlaying
        }) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play voice note",
                tint = MaterialTheme.colorScheme.primaryContainer
            )
        }

        val progressShape = remember(progress) {
            object : Shape {
                override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density) =
                    Outline.Rectangle(Rect(0f, 0f, size.width * progress, size.height))
            }
        }

        Box(
            modifier = Modifier
                .width(180.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(4.dp))
        ) {
            WaveformImage(
                waveformUri = waveformUri,
                modifier = Modifier.fillMaxSize(),
                alpha = 0.3f,
                tint = MaterialTheme.colorScheme.primaryContainer
            )
            WaveformImage(
                waveformUri = waveformUri,
                modifier = Modifier.fillMaxSize().clip(progressShape),
                alpha = 1f,
                tint = MaterialTheme.colorScheme.primaryContainer
            )
        }
    }
}

@Composable
private fun WaveformImage(
    waveformUri: Uri?,
    modifier: Modifier,
    alpha: Float,
    tint: Color
) {
    if (waveformUri != null) {
        AsyncImage(
            model = waveformUri,
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.FillBounds,
            alpha = alpha,
            colorFilter = ColorFilter.tint(tint)
        )
    } else {
        Image(
            painter = painterResource(R.drawable.ic_waveform),
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.FillBounds,
            alpha = alpha,
            colorFilter = ColorFilter.tint(tint)
        )
    }
}
