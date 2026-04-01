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
| Language | Kotlin 2.3.20 |
| UI | Jetpack Compose + Material3 |
| Architecture | MVVM + Clean Architecture (multi-module) |
| Database | Room 2.8.4 (KSP) |
| Dependency Injection | Koin 4.2.0 |
| Image loading | Coil 2.7.0 |
| Navigation | Jetpack Navigation Compose |
| Testing | JUnit 4, MockK, kotlinx-coroutines-test |

---

## Architecture

The app uses **MVVM + Clean Architecture** with reactive Kotlin Flows, organized into feature modules.

```
ModeStateHolder.isWorkMode (StateFlow<Boolean>)
  ↓ flatMapLatest
TodayRepository / WorkTodayRepository
  ↓ Flow<List<Entity>> → map to MessageItem
  ↓ stateIn(viewModelScope)
Composable via collectAsState()
```

### Layers

- **UI** — Composable screens and ViewModels per feature module. Side-effects like camera and permissions are dispatched via `MutableSharedFlow<MainAction>` and handled in `MainActivity`.
- **Domain** — Repository interfaces + `ModeStateHolder`, the single source of truth for the personal/work mode toggle (backed by SharedPreferences).
- **Data** — Room DAOs with entity↔model mapping. Separate tables exist for each mode: `NoteEntity`/`WorkTodayEntity` and `EntryEntity`/`WorkEntryEntity`.
- **DI** — Koin modules per feature, aggregated in the `:app` module.

### Key Design Decisions

**Dual-mode data flow**: All DB queries use `flatMapLatest` on `ModeStateHolder.isWorkMode`, so switching modes instantly swaps the data source with no UI re-query needed.

**Unified event handling**: All UI interactions are expressed as a sealed interface (e.g., `TodayAction`, `SettingsAction`) and routed through `ViewModel.handleEvent()`, keeping event logic centralized.

**Midnight flush**: `MidnightAlarmScheduler` schedules a repeating alarm; `TimeFlushReceiver` calls `flushTodayEntries()` to bulk-move today's notes into the entries table.

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
./gradlew :today:todayImpl:test --tests "com.ijunes.mefirst.today.TodayScreenViewModelTest"

# Run instrumented tests (requires a connected device)
./gradlew connectedAndroidTest
```

---

## Project Structure

The project is organized as a multi-module Gradle build. Feature modules follow a split pattern: a public API module and a private implementation module.

```
mefirst-android/
├── app/                        # Application shell: MainActivity, navigation, top-level DI
│   └── src/main/kotlin/.../
│       ├── di/                 # Koin modules (database, repository, datastore, workers)
│       ├── main/               # MainActivity, MainScreenComposable, AppModeViewModel
│       └── onboarding/         # Onboarding screen
│
├── database/                   # Room database: entities, DAOs, type converters
│   └── src/main/kotlin/.../
│       ├── entity/             # NoteEntity, WorkTodayEntity, EntryEntity, WorkEntryEntity
│       ├── dao/                # TodayDao, WorkTodayDao, EntriesDao, WorkEntriesDao
│       └── converter/          # Room type converters
│
├── common/                     # Shared code used across feature modules
│   └── src/main/kotlin/.../
│       ├── state/              # ModeStateHolder (personal/work mode toggle)
│       ├── components/         # Shared UI components (e.g., VoiceNotePlayer)
│       ├── action/             # MainAction sealed interface
│       ├── data/               # MessageItem data class
│       └── util/               # Utility functions (TimeUtil, etc.)
│
├── ui/                         # Design system: Material3 theme, colors, typography
│
├── today/                      # "Today" feature — daily note capture
│   ├── src/                    # Public API: repository interfaces, ViewModel, UiModel
│   ├── todayImpl/              # Implementation: Room-backed repos, TodayScreen composable
│   └── todayApp/               # App-level wiring for the today feature
│
├── entries/                    # "Entries" feature — historical entries archive
│   ├── src/                    # Public API: repository interfaces, ViewModel, UiModel
│   ├── entriesImpl/            # Implementation: Room-backed repos, EntriesScreen composable
│   └── entriesApp/             # App-level wiring for the entries feature
│
├── settings/                   # Settings feature
│   └── src/main/kotlin/.../
│       ├── alarm/              # MidnightAlarmScheduler, TimeFlushReceiver
│       ├── backup/             # BackupManager
│       ├── pin/                # PIN authentication screen
│       └── presentation/       # SettingsScreen, SettingsViewModel, SettingsUiState
│
gradle/libs.versions.toml       # Version catalog
```
