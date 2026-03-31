package com.ijunes.mefirst.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ijunes.mefirst.database.model.MediaType

@Entity(tableName = "today")
data class NoteEntity(
    @PrimaryKey val timeStamp: Long,
    @ColumnInfo(name = "note_text") val noteText: String? = null,
    @ColumnInfo(name = "media_type") val mediaType: MediaType = MediaType.TEXT,
    @ColumnInfo(name = "media_path") val mediaPath: String? = null,
    @ColumnInfo(name = "waveform_path") val waveformPath: String? = null
)

@Entity(tableName = "work_today")
data class WorkTodayEntity(
    @PrimaryKey val timeStamp: Long,
    @ColumnInfo(name = "note_text") val noteText: String? = null,
    @ColumnInfo(name = "media_type") val mediaType: MediaType = MediaType.TEXT,
    @ColumnInfo(name = "media_path") val mediaPath: String? = null,
    @ColumnInfo(name = "waveform_path") val waveformPath: String? = null
)
