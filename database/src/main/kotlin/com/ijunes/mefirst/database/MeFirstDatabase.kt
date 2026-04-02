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

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Recreate today with UUID PK
        db.execSQL("""
            CREATE TABLE `today_new` (
                `id` TEXT NOT NULL,
                `timeStamp` INTEGER NOT NULL,
                `note_text` TEXT,
                `media_type` INTEGER NOT NULL DEFAULT 0,
                `media_path` TEXT,
                `waveform_path` TEXT,
                `mode` TEXT NOT NULL DEFAULT 'PERSONAL',
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
        db.execSQL("INSERT INTO `today_new` SELECT lower(hex(randomblob(16))), timeStamp, note_text, media_type, media_path, waveform_path, mode FROM `today`")
        db.execSQL("DROP TABLE `today`")
        db.execSQL("ALTER TABLE `today_new` RENAME TO `today`")

        // Recreate entries with UUID PK
        db.execSQL("""
            CREATE TABLE `entries_new` (
                `id` TEXT NOT NULL,
                `timeStamp` INTEGER NOT NULL,
                `entry_text` TEXT,
                `media_type` INTEGER NOT NULL DEFAULT 0,
                `media_path` TEXT,
                `waveform_path` TEXT,
                `mode` TEXT NOT NULL DEFAULT 'PERSONAL',
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
        db.execSQL("INSERT INTO `entries_new` SELECT lower(hex(randomblob(16))), timeStamp, entry_text, media_type, media_path, waveform_path, mode FROM `entries`")
        db.execSQL("DROP TABLE `entries`")
        db.execSQL("ALTER TABLE `entries_new` RENAME TO `entries`")
    }
}

@Database(
    entities = [NoteEntity::class, EntryEntity::class],
    version = 4
)
@TypeConverters(Converters::class)
abstract class MeFirstDatabase : RoomDatabase() {

    abstract fun todayDao(): TodayDao

    abstract fun entriesDao(): EntriesDao
}
