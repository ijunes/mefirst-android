package com.ijunes.mefirst.common.data

import android.net.Uri
import com.ijunes.mefirst.common.data.model.MediaType

data class MessageItem(
    val timeStamp: Long,
    val text: String? = null,
    val mediaType: MediaType = MediaType.TEXT,
    val mediaPath: Uri? = null,
    val waveformPath: Uri? = null,
)