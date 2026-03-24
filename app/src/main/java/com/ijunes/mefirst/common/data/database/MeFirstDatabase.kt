package com.ijunes.mefirst.common.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ijunes.mefirst.common.data.converter.Converters
import com.ijunes.mefirst.entries.data.EntriesDao
import com.ijunes.mefirst.today.data.dao.TodayDao
import com.ijunes.mefirst.entries.data.WorkEntriesDao
import com.ijunes.mefirst.today.data.dao.WorkTodayDao
import com.ijunes.mefirst.entries.data.EntryEntity
import com.ijunes.mefirst.today.data.NoteEntity
import com.ijunes.mefirst.entries.data.WorkEntryEntity
import com.ijunes.mefirst.today.data.WorkTodayEntity

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
