package com.ijunes.mefirst.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ijunes.mefirst.database.converter.Converters
import com.ijunes.mefirst.database.entity.EntryEntity
import com.ijunes.mefirst.database.entity.NoteEntity
import com.ijunes.mefirst.data.dao.EntriesDao
import com.ijunes.mefirst.data.dao.TodayDao

@Database(
    entities = [NoteEntity::class, EntryEntity::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class MeFirstDatabase : RoomDatabase() {

    abstract fun todayDao(): TodayDao

    abstract fun entriesDao(): EntriesDao
}
