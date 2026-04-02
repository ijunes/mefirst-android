package com.ijunes.mefirst.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ijunes.mefirst.database.model.MediaType
import com.ijunes.mefirst.database.model.NoteMode
import java.util.UUID

@Entity(tableName = "entries")
data class EntryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val timeStamp: Long,
    @ColumnInfo(name = "entry_text") val text: String? = null,
    @ColumnInfo(name = "media_type") val mediaType: MediaType = MediaType.TEXT,
    @ColumnInfo(name = "media_path") val mediaPath: String? = null,
    @ColumnInfo(name = "waveform_path") val waveformPath: String? = null,
    val mode: NoteMode = NoteMode.PERSONAL,
)
