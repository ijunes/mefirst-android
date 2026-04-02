package com.ijunes.mefirst.today.recording

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream

private const val TAG = "AudioRecordingManager"
private const val WAVEFORM_WIDTH = 400
private const val WAVEFORM_HEIGHT = 100

data class RecordingResult(
    val audioFile: File,
    val waveformFile: File?,
    val samples: List<Int>,
)

class AudioRecordingManager(private val application: Application) {

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private var mediaRecorder: MediaRecorder? = null
    private var recordingFile: File? = null
    private val amplitudeSamples = mutableListOf<Int>()

    fun start() {
        val dir = application.getExternalFilesDir(Environment.DIRECTORY_MUSIC) ?: application.filesDir
        val file = File(dir, "voice_${System.currentTimeMillis()}.m4a")
        recordingFile = file

        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(application)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        recorder.setOutputFile(file.absolutePath)
        recorder.prepare()
        recorder.start()
        mediaRecorder = recorder
        amplitudeSamples.clear()
        _isRecording.value = true
    }

    fun sampleAmplitude() {
        amplitudeSamples.add(mediaRecorder?.maxAmplitude ?: 0)
    }

    fun stop(): RecordingResult? {
        val file = recordingFile ?: return null
        val samples = amplitudeSamples.toList()
        amplitudeSamples.clear()
        recordingFile = null

        return try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
            _isRecording.value = false
            val waveformFile = generateWaveformBitmap(file.parentFile ?: file, samples)
            RecordingResult(file, waveformFile, samples)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording", e)
            _isRecording.value = false
            null
        }
    }

    fun release() {
        try {
            if (_isRecording.value) mediaRecorder?.stop()
        } catch (e: Exception) {
            Log.w(TAG, "Exception while stopping recorder on release", e)
        }
        mediaRecorder?.release()
        mediaRecorder = null
        _isRecording.value = false
    }

    fun generateWaveformBitmap(dir: File, amplitudes: List<Int>): File? {
        if (amplitudes.isEmpty()) return null
        val bitmap = createBitmap(WAVEFORM_WIDTH, WAVEFORM_HEIGHT)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val maxAmp = amplitudes.max().coerceAtLeast(1)
        val barCount = amplitudes.size
        val stride = WAVEFORM_WIDTH.toFloat() / barCount
        val barWidth = stride * 0.7f

        amplitudes.forEachIndexed { i, amp ->
            val barHeight = ((amp.toFloat() / maxAmp) * (WAVEFORM_HEIGHT - 8)).coerceAtLeast(4f)
            val x = i * stride
            val y = (WAVEFORM_HEIGHT - barHeight) / 2f
            canvas.drawRoundRect(RectF(x, y, x + barWidth, y + barHeight), barWidth / 2, barWidth / 2, paint)
        }

        return try {
            val file = File(dir, "waveform_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
            }
            file
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write waveform bitmap", e)
            null
        } finally {
            bitmap.recycle()
        }
    }
}
