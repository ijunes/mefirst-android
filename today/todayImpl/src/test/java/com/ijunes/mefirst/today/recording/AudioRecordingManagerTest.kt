package com.ijunes.mefirst.today.recording

import android.app.Application
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.File

class AudioRecordingManagerTest {

    private lateinit var mockApp: Application
    private lateinit var manager: AudioRecordingManager

    @Before
    fun setUp() {
        mockApp = mockk(relaxed = true) {
            every { filesDir } returns File(System.getProperty("java.io.tmpdir")!!)
        }
        manager = AudioRecordingManager(mockApp)
    }

    @Test
    fun `isRecording is false initially`() {
        assertFalse(manager.isRecording.value)
    }

    @Test
    fun `stop returns null when not recording`() {
        assertNull(manager.stop())
    }

    @Test
    fun `generateWaveformBitmap returns null for empty samples`() {
        val dir = File(System.getProperty("java.io.tmpdir")!!)
        assertNull(manager.generateWaveformBitmap(dir, emptyList()))
    }
}
