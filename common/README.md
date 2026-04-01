# :common

Shared types, state holders, utilities, and UI components consumed across multiple feature modules. No feature-specific logic lives here.

## Contents

### State holders

All state holders are backed by `SharedPreferences` (key `"app_prefs"`) and are registered as Koin singletons in `:app`'s `repositoryModule`.

| Class | Purpose |
|---|---|
| `ModeStateHolder` | Persists and exposes `isWorkMode: StateFlow<Boolean>`. Call `setWorkMode(Boolean)` to toggle between personal and work feeds. |
| `OnboardingStateHolder` | Tracks whether the user has completed onboarding. `isOnboardingComplete` is a plain `Boolean` property; call `markComplete()` to set it. |
| `SettingsStateHolder` | Persists flush time (`flushHour`, `flushMinute` as `StateFlow<Int>`), and an optional SHA-256-hashed PIN. Exposes `setFlushTime()`, `setPin()`, `removePin()`, and `verifyPin()`. |

### Actions

| Class | Purpose |
|---|---|
| `MainAction` | Sealed interface of all user-initiated events originating from the Today screen. Dispatched to `TodayViewModel.handleEvent()`. |

#### MainAction variants

| Variant | Payload | Effect |
|---|---|---|
| `SendChat` | `text: String` | Send text note, commit pending image, or toggle voice recording |
| `DeleteToday` | — | Clear all of today's notes for the active mode |
| `OpenGallery` | — | Open system image picker |
| `OpenCamera` | — | Open camera to capture a photo |
| `ClearPendingImage` | — | Discard the staged image URI |
| `SetWorkMode` | `isWork: Boolean` | Switch between personal and work data |

### Data models

| Class | Purpose |
|---|---|
| `Message` | Unified display model for a single note. Carries `timeStamp`, optional `text`, `mediaType` (`TEXT`, `VOICE`, or `IMAGE`), `mediaPath`, and `waveformPath`. |

### Utilities

| Function | Purpose |
|---|---|
| `Long.toDateString()` | Formats an epoch-ms timestamp as `"EEEE MMM dd, yyyy"` using the device locale. |
| `Long.toTimeString()` | Formats an epoch-ms timestamp as `"hh:mm a"` using the device locale. |

### Components

| Composable | Purpose |
|---|---|
| `VoiceNotePlayer` | Renders a play/pause button alongside an animated waveform bar for a voice note URI. Manages a `MediaPlayer` internally and updates playback progress every 100 ms via a coroutine. Falls back to a static waveform drawable when no `waveformUri` is provided. |

## Dependencies

`:common` has no dependency on any other project module. Feature modules depend on `:common`, never the reverse.
