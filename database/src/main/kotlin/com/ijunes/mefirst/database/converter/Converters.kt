package com.ijunes.mefirst.database.converter

import androidx.room.TypeConverter
import com.ijunes.mefirst.database.model.MediaType
import com.ijunes.mefirst.database.model.NoteMode

class Converters {

    @TypeConverter
    fun toMediaType(ordinalValue: Int) = if (ordinalValue in 0 until MediaType.entries.size) MediaType.entries[ordinalValue] else MediaType.TEXT

    @TypeConverter
    fun mediaTypeToInt(mediaType: MediaType) = mediaType.ordinal

    @TypeConverter
    fun toNoteMode(value: String): NoteMode = NoteMode.valueOf(value)

    @TypeConverter
    fun noteModeToString(mode: NoteMode): String = mode.name

}
