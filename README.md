# MeFirst

A focused Android app for capturing daily notes with **personal/work mode separation**. Notes taken throughout the day are automatically flushed to a historical log at midnight, keeping your daily slate clean.

Personal Mode  |  Work Mode    | Settings
:-------------------------:|:-------------------------:|:-------------------------:
![Today Entry Screen](/screenshots/home.jpg "Today Entry Screen")  |  ![Today Entry Screen in Work Mode](/screenshots/work.jpg "Today Entry Screen in Work Mode")  |  ![Settings Screen](/screenshots/settings.jpg "Settings Screen")

---

## Features

- **Dual-mode notes** — toggle between Personal and Work mode; each mode has its own completely separate note and entry history
- **Daily flush** — a midnight alarm automatically archives that day's notes into a permanent entries log
- **Voice notes with waveform** — record audio notes; a waveform visualization is rendered from live amplitude sampling during recording
- **Photo attachments** — attach camera photos directly to notes
- **Bottom navigation** — three tabs: Today, Entries, Settings

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material3 |
| Architecture | MVVM + Clean Architecture |
| Database | Room 2.8.4 (KSP) |
| Dependency Injection | Koin 4.2.0 |
| Image loading | Coil 2.7.0 |
| Navigation | Jetpack Navigation Compose |
| Testing | JUnit 4, MockK, kotlinx-coroutines-test |

---

## Architecture

The app uses **MVVM + Clean Architecture** with reactive Kotlin Flows.

```
ModeStateHolder.isWorkMode (StateFlow<Boolean>)
  ↓ flatMapLatest
TodayRepository / WorkTodayRepository
  ↓ Flow<List<Entity>> → map to MessageItem
  ↓ stateIn(viewModelScope)
Composable via collectAsState()
```

### Layers

- **UI** (`ui/`) — Composable screens and ViewModels. Side-effects like camera and permissions are dispatched via `MutableSharedFlow<ActivityCommand>` and handled in `MainActivity`.
- **Domain** — Repository interfaces + `ModeStateHolder`, the single source of truth for the personal/work mode toggle (backed by SharedPreferences).
- **Data** (`data/`) — Room DAOs with entity↔model mapping. Separate tables exist for each mode: `NoteEntity`/`WorkNoteEntity` and `EntryEntity`/`WorkEntryEntity`.
- **DI** (`di/`) — Koin modules: `DataBaseModule`, `RepositoryModule`.

### Key Design Decisions

**Dual-mode data flow**: All DB queries use `flatMapLatest` on `ModeStateHolder.isWorkMode`, so switching modes instantly swaps the data source with no UI re-query needed.

**Unified event handling**: All UI interactions are expressed as a `MainEvent` sealed interface and routed through `ViewModel.handleEvent()`, keeping event logic centralized.

**Midnight flush**: `MidnightAlarmScheduler` schedules a repeating alarm; the `BroadcastReceiver` calls `flushTodayEntries()` to bulk-move today's notes into the entries table.

**Voice waveform rendering**: `MediaRecorder` samples amplitude every 100ms during recording; the waveform bitmap is rendered when recording stops.

### Navigation

Bottom nav with three destinations (`today`, `entries`, `settings`) via Jetpack Compose `NavHost` in `MainScreenComposable.kt`, using `saveState`/`restoreState` and `launchSingleTop`.

---

## Building

Requirements: Android Studio, JDK 17+, Android SDK.

```bash
# Build the project
./gradlew build

# Install to a connected device or emulator
./gradlew installDebug

# Run unit tests (JVM)
./gradlew test

# Run a single test class
./gradlew test --tests "com.ijunes.mefirst.TodayScreenViewModelTest"

# Run instrumented tests (requires a connected device)
./gradlew connectedAndroidTest
```

---

## Project Structure

```
app/src/main/java/com/ijunes/mefirst/
├── ui/                  # Composable screens + ViewModels
├── data/                # Room entities, DAOs, repository implementations
├── domain/              # Repository interfaces, ModeStateHolder
├── di/                  # Koin DI modules
└── MainActivity.kt      # Activity + ActivityCommand handler

app/src/test/            # Unit tests (MockK + UnconfinedTestDispatcher)
gradle/libs.versions.toml  # Version catalog
```
