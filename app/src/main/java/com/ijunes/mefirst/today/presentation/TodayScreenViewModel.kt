package com.ijunes.mefirst.today.presentation

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ijunes.mefirst.common.data.model.MediaType
import com.ijunes.mefirst.today.data.NoteEntity
import com.ijunes.mefirst.today.data.WorkTodayEntity
import com.ijunes.mefirst.common.state.ModeStateHolder
import com.ijunes.mefirst.today.data.repository.TodayRepository
import com.ijunes.mefirst.today.data.repository.TodayRepositoryImpl
import com.ijunes.mefirst.today.data.repository.WorkTodayRepository
import com.ijunes.mefirst.today.data.repository.WorkTodayRepositoryImpl
import com.ijunes.mefirst.common.data.MessageItem
import com.ijunes.mefirst.today.domain.TodayAction
import com.ijunes.mefirst.main.MainAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.inject
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.ExperimentalCoroutinesApi

class TodayScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val personalRepo: TodayRepository by inject(TodayRepositoryImpl::class.java)
    private val workRepo: WorkTodayRepository by inject(WorkTodayRepositoryImpl::class.java)
    private val modeHolder: ModeStateHolder by inject(ModeStateHolder::class.java)

    @OptIn(ExperimentalCoroutinesApi::class)
    val conversation: StateFlow<List<MessageItem>> = modeHolder.isWorkMode
        .flatMapLatest { isWork ->
            if (isWork) {
                workRepo.getAllNotes().map { notes ->
                    notes.map {
                        MessageItem(
                            text = it.noteText ?: "",
                            mediaType = it.mediaType,
                            timeStamp = it.timeStamp,
                            mediaPath = it.mediaPath?.toUri(),
                            waveformPath = it.waveformPath?.toUri()
                        )
                    }
                }
            } else {
                personalRepo.getAllNotes().map { notes ->
                    notes.map {
                        MessageItem(
                            text = it.noteText ?: "",
                            mediaType = it.mediaType,
                            timeStamp = it.timeStamp,
                            mediaPath = it.mediaPath?.toUri(),
                            waveformPath = it.waveformPath?.toUri()
                        )
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    private val _activityCommands = MutableSharedFlow<TodayAction>(extraBufferCapacity = 1)
    val activityCommands: SharedFlow<TodayAction> = _activityCommands.asSharedFlow()

    fun handleEvent(event: MainAction) {
        val app = getApplication<Application>()
        when (event) {
            is MainAction.SendChat -> when {
                event.text.isNotEmpty() -> insertNote(event.text)
                isRecording.value -> stopRecording()
                else -> {
                    if (app.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        startRecording()
                    } else {
                        _activityCommands.tryEmit(TodayAction.RequestRecordPermission)
                    }
                }
            }
            MainAction.DeleteToday -> clearToday()
            MainAction.OpenGallery -> _activityCommands.tryEmit(TodayAction.LaunchGallery)
            MainAction.OpenCamera -> {
                val file = File(app.filesDir, "camera_${System.currentTimeMillis()}.jpg")
                val uri = FileProvider.getUriForFile(app, "${app.packageName}.fileprovider", file)
                _activityCommands.tryEmit(TodayAction.LaunchCamera(uri))
            }
            is MainAction.SetWorkMode -> modeHolder.setWorkMode(event.isWork)
        }
    }

    private var mediaRecorder: MediaRecorder? = null
    private var recordingFile: File? = null
    private val amplitudeSamples = mutableListOf<Int>()
    private var amplitudeSamplingJob: Job? = null

    fun insertNote(msg: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (modeHolder.isWorkMode.value) {
                    workRepo.insertNote(WorkTodayEntity(System.currentTimeMillis(), msg))
                } else {
                    personalRepo.insertNote(NoteEntity(System.currentTimeMillis(), msg))
                }
            }
        }
    }

    fun insertVoiceNote(uri: Uri, waveformUri: Uri? = null) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (modeHolder.isWorkMode.value) {
                    workRepo.insertNote(
                        WorkTodayEntity(
                            System.currentTimeMillis(),
                            mediaType = MediaType.VOICE,
                            mediaPath = uri.toString(),
                            waveformPath = waveformUri?.toString()
                        )
                    )
                } else {
                    personalRepo.insertNote(
                        NoteEntity(
                            System.currentTimeMillis(),
                            mediaType = MediaType.VOICE,
                            mediaPath = uri.toString(),
                            waveformPath = waveformUri?.toString()
                        )
                    )
                }
            }
        }
    }

    fun insertImageNote(uri: Uri) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (modeHolder.isWorkMode.value) {
                    workRepo.insertNote(
                        WorkTodayEntity(
                            System.currentTimeMillis(),
                            mediaType = MediaType.IMAGE,
                            mediaPath = uri.toString()
                        )
                    )
                } else {
                    personalRepo.insertNote(
                        NoteEntity(
                            System.currentTimeMillis(),
                            mediaType = MediaType.IMAGE,
                            mediaPath = uri.toString()
                        )
                    )
                }
            }
        }
    }

    fun startRecording() {
        val app = getApplication<Application>()
        val dir = app.getExternalFilesDir(Environment.DIRECTORY_MUSIC) ?: app.filesDir
        val file = File(dir, "voice_${System.currentTimeMillis()}.m4a")
        recordingFile = file

        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(app)
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
        _isRecording.value = true

        amplitudeSamples.clear()
        amplitudeSamplingJob = viewModelScope.launch {
            while (isActive) {
                amplitudeSamples.add(mediaRecorder?.maxAmplitude ?: 0)
                delay(100)
            }
        }
    }

    fun stopRecording() {
        amplitudeSamplingJob?.cancel()
        amplitudeSamplingJob = null
        val samples = amplitudeSamples.toList()
        amplitudeSamples.clear()

        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
            recordingFile?.let { audioFile ->
                viewModelScope.launch(Dispatchers.IO) {
                    val waveformFile = generateWaveformBitmap(audioFile.parentFile ?: audioFile, samples)
                    insertVoiceNote(Uri.fromFile(audioFile), waveformFile?.let { Uri.fromFile(it) })
                }
            }
        } catch (_: Exception) {
            // recording failed, discard
        } finally {
            recordingFile = null
            _isRecording.value = false
        }
    }

    private fun generateWaveformBitmap(dir: File, amplitudes: List<Int>): File? {
        if (amplitudes.isEmpty()) return null
        val width = 400
        val height = 100
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val maxAmp = amplitudes.max().coerceAtLeast(1)
        val barCount = amplitudes.size
        val stride = width.toFloat() / barCount
        val barWidth = stride * 0.7f

        amplitudes.forEachIndexed { i, amp ->
            val barHeight = ((amp.toFloat() / maxAmp) * (height - 8)).coerceAtLeast(4f)
            val x = i * stride
            val y = (height - barHeight) / 2f
            canvas.drawRoundRect(RectF(x, y, x + barWidth, y + barHeight), barWidth / 2, barWidth / 2, paint)
        }

        return try {
            val file = File(dir, "waveform_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
            }
            file
        } catch (_: Exception) {
            null
        } finally {
            bitmap.recycle()
        }
    }

    fun clearToday() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (modeHolder.isWorkMode.value) {
                    workRepo.flushTodayEntries()
                } else {
                    personalRepo.flushTodayEntries()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        amplitudeSamplingJob?.cancel()
        mediaRecorder?.release()
        mediaRecorder = null
    }
}
