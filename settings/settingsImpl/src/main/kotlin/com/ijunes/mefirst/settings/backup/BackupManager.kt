package com.ijunes.mefirst.settings.backup

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.ijunes.mefirst.database.MeFirstDatabase
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class BackupManager(
    private val context: Context,
    private val database: MeFirstDatabase,
) {
    fun backup(outputUri: Uri) {
        database.openHelper.writableDatabase.execSQL("PRAGMA wal_checkpoint(TRUNCATE)")

        context.contentResolver.openOutputStream(outputUri)?.use { out ->
            ZipOutputStream(BufferedOutputStream(out)).use { zip ->
                val dbFile = context.getDatabasePath("me_first")
                addToZip(zip, dbFile, "db/me_first")
                File("${dbFile.path}-shm").takeIf { it.exists() }
                    ?.let { addToZip(zip, it, "db/me_first-shm") }
                File("${dbFile.path}-wal").takeIf { it.exists() }
                    ?.let { addToZip(zip, it, "db/me_first-wal") }

                context.filesDir.walkTopDown().filter { it.isFile }.forEach { file ->
                    addToZip(zip, file, "files/${file.relativeTo(context.filesDir).path}")
                }

                context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
                    ?.walkTopDown()?.filter { it.isFile }?.forEach { file ->
                        addToZip(zip, file, "ext_music/${file.name}")
                    }
            }
        }
    }

    fun restore(inputUri: Uri) {
        database.close()

        context.contentResolver.openInputStream(inputUri)?.use { inputStream ->
            ZipInputStream(BufferedInputStream(inputStream)).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    val dest: File? = when {
                        entry.name.startsWith("db/") -> {
                            val name = entry.name.removePrefix("db/")
                            File(context.getDatabasePath("me_first").parentFile, name)
                        }
                        entry.name.startsWith("files/") -> {
                            File(context.filesDir, entry.name.removePrefix("files/"))
                        }
                        entry.name.startsWith("ext_music/") -> {
                            val dir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
                            if (dir != null) File(dir, entry.name.removePrefix("ext_music/")) else null
                        }
                        else -> null
                    }
                    if (dest != null) {
                        dest.parentFile?.mkdirs()
                        dest.outputStream().use { zip.copyTo(it) }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        }
    }

    private fun addToZip(zip: ZipOutputStream, file: File, entryName: String) {
        zip.putNextEntry(ZipEntry(entryName))
        file.inputStream().use { it.copyTo(zip) }
        zip.closeEntry()
    }
}
