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
import androidx.lifecycle.viewModelScope
import com.ijunes.mefirst.database.model.MediaType
import com.ijunes.mefirst.database.entity.NoteEntity
import com.ijunes.mefirst.database.entity.WorkTodayEntity
import com.ijunes.mefirst.common.state.ModeStateHolder
import com.ijunes.mefirst.common.data.Message
import com.ijunes.today.domain.TodayAction
import com.ijunes.today.presentation.TodayViewModel
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.inject
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.createBitmap
import com.ijunes.mefirst.common.action.MainAction
import com.ijunes.today.data.TodayRepository
import com.ijunes.today.data.WorkTodayRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map

class TodayScreenViewModelImpl(application: Application) : TodayViewModel(application) {
    private val personalRepo: TodayRepository by inject(TodayRepository::class.java)
    private val workRepo: WorkTodayRepository by inject(WorkTodayRepository::class.java)
    private val modeHolder: ModeStateHolder by inject(ModeStateHolder::class.java)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val conversation: StateFlow<List<Message>> = modeHolder.isWorkMode
        .flatMapLatest { isWork ->
            if (isWork) {
                workRepo.getAllNotes().map { notes ->
                    notes.map {
                        Message(
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
                        Message(
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
    override val isRecording: StateFlow<Boolean> = _isRecording

    private val _activityCommands = MutableSharedFlow<TodayAction>(extraBufferCapacity = 1)
    override val actions: SharedFlow<TodayAction> = _activityCommands.asSharedFlow()

    private val _pendingImageUri = MutableStateFlow<Uri?>(null)
    override val pendingImageUri: StateFlow<Uri?> = _pendingImageUri

    override fun handleEvent(event: MainAction) {
        val app = getApplication<Application>()
        when (event) {
            is MainAction.SendChat -> when {
                event.text.isNotEmpty() -> insertNote(event.text)
                _pendingImageUri.value != null -> commitPendingImage()
                isRecording.value -> stopRecording()
                else -> {
                    if (app.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        startRecording()
                    } else {
                        _activityCommands.tryEmit(TodayAction.RequestRecordPermission)
                    }
                }
            }
            is MainAction.SetWorkMode -> modeHolder.setWorkMode(event.isWork)
            is MainAction.DeleteMessage -> deleteTodayNote(event.message)
            MainAction.ClearPendingImage -> _pendingImageUri.value = null
            MainAction.DeleteToday -> clearToday()
            MainAction.OpenGallery -> _activityCommands.tryEmit(TodayAction.LaunchGallery)
            MainAction.OpenCamera -> {
                val file = File(app.filesDir, "camera_${System.currentTimeMillis()}.jpg")
                val uri = FileProvider.getUriForFile(app, "${app.packageName}.fileprovider", file)
                _activityCommands.tryEmit(TodayAction.LaunchCamera(uri))
            }
        }
    }

    private var mediaRecorder: MediaRecorder? = null
    private var recordingFile: File? = null
    private val amplitudeSamples = mutableListOf<Int>()
    private var amplitudeSamplingJob: Job? = null

    override fun insertNote(msg: String) {
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

    override fun setPendingImage(uri: Uri) {
        _pendingImageUri.value = uri
    }

    private fun commitPendingImage() {
        val uri = _pendingImageUri.value ?: return
        _pendingImageUri.value = null
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

    override fun startRecording() {
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

    fun deleteTodayNote(message: Message) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (modeHolder.isWorkMode.value) {
                    workRepo.deleteTodayNote(message.timeStamp)
                } else {
                    personalRepo.deleteTodayNote(message.timeStamp)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        amplitudeSamplingJob?.cancel()
        if (_isRecording.value) {
            try {
                mediaRecorder?.stop()
            } catch (_: Exception) {}
        }
        mediaRecorder?.release()
        mediaRecorder = null
    }
}
