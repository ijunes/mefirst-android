package com.ijunes.mefirst.today.presentation

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import com.ijunes.mefirst.common.action.MainAction
import com.ijunes.mefirst.common.data.Message
import com.ijunes.mefirst.common.state.ModeStateHolder
import com.ijunes.mefirst.database.entity.NoteEntity
import com.ijunes.mefirst.database.model.MediaType
import com.ijunes.mefirst.database.model.NoteMode
import com.ijunes.mefirst.today.recording.AudioRecordingManager
import com.ijunes.today.data.TodayRepository
import com.ijunes.today.domain.TodayAction
import com.ijunes.today.presentation.TodayViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// How long a StateFlow stays active after the last subscriber unsubscribes, giving
// the UI time to resubscribe during configuration changes without restarting the query.
private const val FLOW_TIMEOUT_MS = 5_000L

class TodayScreenViewModelImpl(
    application: Application,
    private val repo: TodayRepository,
    private val modeHolder: ModeStateHolder,
    private val audioRecordingManager: AudioRecordingManager,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : TodayViewModel(application) {

    private val activeMode: NoteMode
        get() = if (modeHolder.isWorkMode.value) NoteMode.WORK else NoteMode.PERSONAL

    @OptIn(ExperimentalCoroutinesApi::class)
    override val conversation: StateFlow<List<Message>> = modeHolder.isWorkMode
        .flatMapLatest { isWork ->
            val mode = if (isWork) NoteMode.WORK else NoteMode.PERSONAL
            repo.getAllNotes(mode).map { notes ->
                notes.map {
                    Message(
                        id = it.id,
                        text = it.noteText ?: "",
                        mediaType = it.mediaType,
                        timeStamp = it.timeStamp,
                        mediaPath = it.mediaPath?.let { p -> Uri.parse(p) },
                        waveformPath = it.waveformPath?.let { p -> Uri.parse(p) }
                    )
                }
            }
        }
        .catch { e -> Log.e("TodayViewModel", "Failed to load notes", e) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(FLOW_TIMEOUT_MS), emptyList())

    override val isRecording: StateFlow<Boolean> = audioRecordingManager.isRecording

    private val _activityCommands = MutableSharedFlow<TodayAction>(extraBufferCapacity = 64)
    override val actions: SharedFlow<TodayAction> = _activityCommands.asSharedFlow()

    private val _pendingImageUri = MutableStateFlow<Uri?>(null)
    override val pendingImageUri: StateFlow<Uri?> = _pendingImageUri

    private fun launchIO(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                withContext(ioDispatcher) { block() }
            } catch (e: Exception) {
                Log.e("TodayViewModel", "IO operation failed", e)
            }
        }
    }

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

    private var amplitudeSamplingJob: Job? = null

    override fun insertNote(msg: String) {
        launchIO {
            repo.insertNote(NoteEntity(timeStamp = System.currentTimeMillis(), noteText = msg, mode = activeMode))
        }
    }

    override fun setPendingImage(uri: Uri) {
        _pendingImageUri.value = uri
    }

    private fun commitPendingImage() {
        val uri = _pendingImageUri.value ?: return
        _pendingImageUri.value = null
        launchIO {
            repo.insertNote(NoteEntity(timeStamp = System.currentTimeMillis(), mediaType = MediaType.IMAGE, mediaPath = uri.toString(), mode = activeMode))
        }
    }

    override fun startRecording() {
        audioRecordingManager.start()
        amplitudeSamplingJob = viewModelScope.launch {
            while (isActive) {
                audioRecordingManager.sampleAmplitude()
                delay(100)
            }
        }
    }

    fun stopRecording() {
        amplitudeSamplingJob?.cancel()
        amplitudeSamplingJob = null
        val mode = activeMode
        val result = audioRecordingManager.stop() ?: return

        launchIO {
            val app = getApplication<Application>()
            val authority = "${app.packageName}.fileprovider"
            val audioUri = FileProvider.getUriForFile(app, authority, result.audioFile)
            val waveformUri = result.waveformFile?.let { FileProvider.getUriForFile(app, authority, it) }
            repo.insertNote(NoteEntity(
                timeStamp = System.currentTimeMillis(),
                mediaType = MediaType.VOICE,
                mediaPath = audioUri.toString(),
                waveformPath = waveformUri?.toString(),
                mode = mode,
            ))
        }
    }

    fun clearToday() {
        launchIO {
            repo.flushTodayEntries(activeMode)
        }
    }

    fun deleteTodayNote(message: Message) {
        launchIO {
            repo.deleteTodayNote(message.id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        amplitudeSamplingJob?.cancel()
        audioRecordingManager.release()
    }
}
