# :today

The Today feature is split across three modules to enforce strict dependency boundaries. The `:app` module only depends on the API module and the wiring module — it never references implementation details directly.

## Module Structure

```
:today          (API)   — interfaces, abstract classes, UI models
:today:todayImpl (impl) — concrete implementations
:today:todayApp  (wiring) — Koin bindings, depends on both above
```

### :today (API module)

Public contracts consumed by `:app`. Contains no implementation logic.

| Type | Class | Purpose |
|---|---|---|
| Abstract ViewModel | `TodayViewModel` | Exposes state flows, pending-image state, and the event handler to the host Activity |
| Screen provider interface | `TodayScreenProvider` | Composable contract for rendering the Today screen |
| UI model | `TodayScreenUiModel` | Data passed into the Today screen, including the message list and any staged image URI |
| Activity commands | `TodayAction` | Sealed interface for one-shot commands emitted to the Activity (`RequestRecordPermission`, `LaunchGallery`, `LaunchCamera`) |
| Repository interfaces | `TodayRepository`, `WorkTodayRepository` | Data access contracts for personal and work-mode notes |

#### TodayViewModel state & entry points

| Member | Type | Description |
|---|---|---|
| `conversation` | `StateFlow<List<Message>>` | Ordered note feed for the active mode; switches source automatically when work mode changes |
| `isRecording` | `StateFlow<Boolean>` | `true` while a voice recording is in progress |
| `activityCommands` | `SharedFlow<TodayAction>` | One-shot commands for the host Activity |
| `pendingImageUri` | `StateFlow<Uri?>` | Staged image URI, non-null after gallery/camera selection, cleared on send or discard |
| `handleEvent(MainAction)` | — | Single entry point for all UI events |
| `startRecording()` | — | Called by the Activity after `RECORD_AUDIO` permission is granted |
| `setPendingImage(Uri)` | — | Called by the Activity after a gallery/camera launcher returns a result |
| `insertNote(String)` | — | Persists a plain-text note immediately |

#### handleEvent dispatch rules

| Event | Effect |
|---|---|
| `SendChat` with non-empty text | Persists a text note |
| `SendChat` with empty text + pending image | Commits `pendingImageUri` to the database |
| `SendChat` with empty text + no pending image | Toggles voice recording (requests permission if not granted) |
| `ClearPendingImage` | Discards `pendingImageUri` |
| `DeleteToday` | Clears all of today's notes for the active mode |
| `OpenGallery` / `OpenCamera` | Emits the corresponding `TodayAction` on `activityCommands` |
| `SetWorkMode(isWork)` | Switches the active data source and feed |

### :today:todayImpl (implementation module)

Concrete implementations. `:app` never depends on this module directly.

| Type | Class | Purpose |
|---|---|---|
| ViewModel | `TodayScreenViewModelImpl` | Implements `TodayViewModel`; handles note insertion, voice recording, pending-image staging, and mode switching |
| Screen | `TodayScreen` | Composable that renders the date header, chat-style note feed, and `ChatBox` input area |
| Screen provider | `TodayScreenProviderImpl` | Implements `TodayScreenProvider` by delegating to `TodayScreen` |
| Repositories | `TodayRepositoryImpl`, `WorkTodayRepositoryImpl` | Implement the repository interfaces against the Room database |
| Message item | `MessageItem` | Composable that renders a single note bubble — text, image (`AsyncImage`), or voice (`VoiceNotePlayer`) |

#### TodayScreen composables

| Composable | Purpose |
|---|---|
| `TodayScreen` | Root layout; pins the feed above `ChatBox`, auto-scrolls to the latest message |
| `ChatBox` | Input bar; shows a pending-image thumbnail when applicable and routes to `ChatTextField` + send button |
| `ChatTextField` | Pill-shaped text field with gallery and camera attachment icons in the trailing slot |
| `DateHeader` | Centred date label displayed at the top of the feed |

#### Voice recording internals (`TodayScreenViewModelImpl`)

1. `startRecording()` — creates an `.m4a` file in external Music storage, starts `MediaRecorder`, launches a coroutine that samples `maxAmplitude` every 100 ms.
2. `stopRecording()` — stops `MediaRecorder`, converts the amplitude samples to a PNG waveform bitmap via `generateWaveformBitmap()`, then calls `insertVoiceNote()` with both URIs.
3. `insertVoiceNote(uri, waveformUri?)` — persists a `VOICE`-typed note to the active mode's repository.

#### Work-mode switching

`TodayScreenViewModelImpl` injects `ModeStateHolder` from `:common`. `conversation` uses `flatMapLatest` on `ModeStateHolder.isWorkMode` to switch between `TodayRepository` (personal) and `WorkTodayRepository` (work) without re-subscribing the collector.

### :today:todayApp (wiring module)

Owns all Koin registrations for the feature. The only module that imports from both `:today` and `:today:todayImpl`.

```kotlin
val todayModule = module {
    factory<TodayRepository>      { TodayRepositoryImpl(get()) }
    factory<WorkTodayRepository>  { WorkTodayRepositoryImpl(get()) }
    viewModel<TodayViewModel>     { TodayScreenViewModelImpl(androidApplication()) }
    single<TodayScreenProvider>   { TodayScreenProviderImpl() }
}
```

## Dependency Graph

```
:app
 ├── :today              (types only — TodayViewModel, TodayScreenProvider, TodayAction)
 └── :today:todayApp     (Koin module — todayModule)

:today:todayApp
 ├── :today
 └── :today:todayImpl

:today:todayImpl
 └── :today
```

## Data Flow

```
MainActivity
 │  injects TodayViewModel (Koin) ──► TodayScreenViewModelImpl
 │  injects TodayScreenProvider (Koin) ──► TodayScreenProviderImpl
 │
 └── MainScreen(todayScreenProvider, uiState, onEvent)
      └── todayScreenProvider.Content(...) ──► TodayScreen (from :todayImpl)
```

The Activity collects `TodayViewModel.activityCommands` to handle `TodayAction` events (permission requests, launcher launches) that cannot be performed inside a Composable. Launcher results are fed back via `TodayViewModel.setPendingImage()` and `TodayViewModel.startRecording()`.
