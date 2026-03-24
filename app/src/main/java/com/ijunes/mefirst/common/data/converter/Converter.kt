package com.ijunes.mefirst.common.data.converter

import androidx.room.TypeConverter
import com.ijunes.mefirst.common.data.model.MediaType

class Converters {
        
        @TypeConverter
        fun toMediaType(ordinalValue: Int) = if (ordinalValue in 0 until MediaType.entries.size) MediaType.entries[ordinalValue] else MediaType.TEXT

        @TypeConverter
        fun mediaTypeToInt(mediaType: MediaType) = mediaType.ordinal

}