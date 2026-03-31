package com.ijunes.mefirst.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ijunes.mefirst.database.converter.Converters
import com.ijunes.mefirst.database.entity.EntryEntity
import com.ijunes.mefirst.database.entity.NoteEntity
import com.ijunes.mefirst.database.entity.WorkEntryEntity
import com.ijunes.mefirst.database.entity.WorkTodayEntity
import com.ijunes.mefirst.data.dao.EntriesDao
import com.ijunes.mefirst.data.dao.TodayDao
import com.ijunes.mefirst.data.dao.WorkEntriesDao
import com.ijunes.mefirst.data.dao.WorkTodayDao

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `work_today` " +
            "(`timeStamp` INTEGER NOT NULL, `note_text` TEXT, `media_type` INTEGER NOT NULL DEFAULT 0, " +
            "`media_path` TEXT, `waveform_path` TEXT, PRIMARY KEY(`timeStamp`))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `work_entries` " +
            "(`timeStamp` INTEGER NOT NULL, `entry_text` TEXT, `media_type` INTEGER NOT NULL DEFAULT 0, " +
            "`media_path` TEXT, `waveform_path` TEXT, PRIMARY KEY(`timeStamp`))"
        )
    }
}

@Database(
    entities = [NoteEntity::class, EntryEntity::class, WorkTodayEntity::class, WorkEntryEntity::class],
    version = 2
)
@TypeConverters(Converters::class)
abstract class MeFirstDatabase : RoomDatabase() {

    abstract fun todayDao(): TodayDao

    abstract fun entriesDao(): EntriesDao

    abstract fun workTodayDao(): WorkTodayDao

    abstract fun workEntriesDao(): WorkEntriesDao
}
