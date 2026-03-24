package com.ijunes.mefirst.entries.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ijunes.mefirst.common.data.model.MediaType

@Entity(tableName = "entries")
data class EntryEntity(
        @PrimaryKey val timeStamp: Long,
        @ColumnInfo(name = "entry_text") val text: String? = null,
        @ColumnInfo(name = "media_type") val mediaType: MediaType = MediaType.TEXT,
        @ColumnInfo(name = "media_path") val mediaPath: String? = null,
        @ColumnInfo(name = "waveform_path") val waveformPath: String? = null
)

@Entity(tableName = "work_entries")
data class WorkEntryEntity(
    @PrimaryKey val timeStamp: Long,
    @ColumnInfo(name = "entry_text") val text: String? = null,
    @ColumnInfo(name = "media_type") val mediaType: MediaType = MediaType.TEXT,
    @ColumnInfo(name = "media_path") val mediaPath: String? = null,
    @ColumnInfo(name = "waveform_path") val waveformPath: String? = null
)

