package com.ijunes.mefirst.settings.backup

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.ijunes.mefirst.database.MeFirstDatabase
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class BackupManagerTest {

    private lateinit var mockContext: Context
    private lateinit var mockDatabase: MeFirstDatabase
    private lateinit var mockContentResolver: ContentResolver
    private lateinit var manager: BackupManager
    private lateinit var tmpDir: File

    @Before
    fun setUp() {
        tmpDir = File(System.getProperty("java.io.tmpdir")!!, "backup_test_${System.currentTimeMillis()}").also { it.mkdirs() }
        mockContext = mockk(relaxed = true)
        mockDatabase = mockk(relaxed = true)
        mockContentResolver = mockk(relaxed = true)
        every { mockContext.contentResolver } returns mockContentResolver
        manager = BackupManager(mockContext, mockDatabase)
    }

    @After
    fun tearDown() {
        tmpDir.deleteRecursively()
    }

    @Test
    fun `backup creates valid ZIP containing db and files entries`() {
        val filesDir = File(tmpDir, "files").also { it.mkdirs() }
        File(filesDir, "note.txt").writeText("hello")
        val dbFile = File(tmpDir, "me_first").also { it.writeText("db-content") }

        every { mockContext.filesDir } returns filesDir
        every { mockContext.getDatabasePath("me_first") } returns dbFile
        every { mockContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC) } returns null

        val outputStream = ByteArrayOutputStream()
        val uri = mockk<Uri>()
        every { mockContentResolver.openOutputStream(uri) } returns outputStream

        manager.backup(uri)

        val entryNames = mutableSetOf<String>()
        ZipInputStream(ByteArrayInputStream(outputStream.toByteArray())).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                entryNames.add(entry.name)
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }

        assertTrue("db/me_first" in entryNames)
        assertTrue("files/note.txt" in entryNames)
    }

    @Test
    fun `restore extracts files entry to correct location`() {
        val filesDir = File(tmpDir, "restore_files").also { it.mkdirs() }
        val dbDir = File(tmpDir, "databases").also { it.mkdirs() }
        val dbFile = File(dbDir, "me_first")

        every { mockContext.filesDir } returns filesDir
        every { mockContext.getDatabasePath("me_first") } returns dbFile
        every { mockContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC) } returns null

        val zipBytes = ByteArrayOutputStream().also { baos ->
            ZipOutputStream(baos).use { zip ->
                zip.putNextEntry(ZipEntry("files/restored.txt"))
                zip.write("restored content".toByteArray())
                zip.closeEntry()
            }
        }.toByteArray()

        val uri = mockk<Uri>()
        every { mockContentResolver.openInputStream(uri) } returns ByteArrayInputStream(zipBytes)

        manager.restore(uri)

        assertTrue(File(filesDir, "restored.txt").exists())
    }

    @Test
    fun `restore does not create files for corrupt ZIP`() {
        val filesDir = File(tmpDir, "corrupt_restore").also { it.mkdirs() }
        every { mockContext.filesDir } returns filesDir
        every { mockContext.getDatabasePath("me_first") } returns File(tmpDir, "me_first")

        val uri = mockk<Uri>()
        every { mockContentResolver.openInputStream(uri) } returns ByteArrayInputStream("not a zip".toByteArray())

        manager.restore(uri)

        assertTrue("No files should be extracted from a corrupt ZIP", filesDir.listFiles()?.isEmpty() != false)
    }
}
