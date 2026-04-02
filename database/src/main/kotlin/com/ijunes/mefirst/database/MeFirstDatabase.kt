package com.ijunes.mefirst.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ijunes.mefirst.database.converter.Converters
import com.ijunes.mefirst.database.entity.EntryEntity
import com.ijunes.mefirst.database.entity.NoteEntity
import com.ijunes.mefirst.data.dao.EntriesDao
import com.ijunes.mefirst.data.dao.TodayDao

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

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `today` ADD COLUMN `mode` TEXT NOT NULL DEFAULT 'PERSONAL'")
        db.execSQL("INSERT INTO `today` (timeStamp, note_text, media_type, media_path, waveform_path, mode) SELECT timeStamp, note_text, media_type, media_path, waveform_path, 'WORK' FROM `work_today`")
        db.execSQL("DROP TABLE `work_today`")
        db.execSQL("ALTER TABLE `entries` ADD COLUMN `mode` TEXT NOT NULL DEFAULT 'PERSONAL'")
        db.execSQL("INSERT INTO `entries` (timeStamp, entry_text, media_type, media_path, waveform_path, mode) SELECT timeStamp, entry_text, media_type, media_path, waveform_path, 'WORK' FROM `work_entries`")
        db.execSQL("DROP TABLE `work_entries`")
    }
}

@Database(
    entities = [NoteEntity::class, EntryEntity::class],
    version = 3
)
@TypeConverters(Converters::class)
abstract class MeFirstDatabase : RoomDatabase() {

    abstract fun todayDao(): TodayDao

    abstract fun entriesDao(): EntriesDao
}
