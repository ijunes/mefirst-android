# :app

The application module. Owns the `Application` class, `MainActivity`, the root `MainScreen` composable, and all Koin wiring that does not belong to a specific feature.

## Responsibilities

- Initialises Koin on startup and loads all feature modules.
- Schedules the daily flush alarm via `AlarmScheduler` at the configured flush time.
- Handles onboarding and PIN gate before showing the main UI.
- Dispatches `TodayAction` commands (permission requests, gallery/camera launchers, backup/restore pickers) to the correct `ActivityResultLauncher`.
- Handles incoming share intents (`ACTION_SEND`) by forwarding images to `TodayViewModel.setPendingImage()` and plain text to `TodayViewModel.insertNote()`.

## Key classes

| Class | Purpose |
|---|---|
| `MeFirstApplication` | `Application` subclass; starts Koin, loads all modules, schedules the flush alarm |
| `MainActivity` | Single activity; owns all `ActivityResultLauncher`s, collects `activityCommands` from `TodayViewModel` and `SettingsViewModel`, and drives the Compose UI |
| `MainScreen` | Root Scaffold composable with a top app bar, `SegmentedModeSelector`, bottom nav bar (Today / Entries / Settings), and a `NavHost` that delegates screen content to provider interfaces |
| `MainScreenUiState` | Aggregated UI state combining `conversation`, `entries`, `isRecording`, `isWorkMode`, and `pendingImageUri` from the active ViewModels |
| `AppModeViewModel` | Thin `AndroidViewModel` that exposes `ModeStateHolder.isWorkMode` to the Compose state pipeline |

## Koin modules loaded at startup

| Module | Source | Contents |
|---|---|---|
| `databaseModule` | `:app` | `MeFirstDatabase` singleton + all four DAO singletons |
| `repositoryModule` | `:app` | `ModeStateHolder`, `OnboardingStateHolder`, `SettingsStateHolder` singletons |
| `todayModule` | `:today:todayApp` | `TodayRepository`, `WorkTodayRepository`, `TodayViewModel`, `TodayScreenProvider` |
| `entriesModule` | `:entries:entriesApp` | `EntriesRepository`, `WorkEntriesRepository`, `EntriesViewModel`, `EntriesScreenProvider` |
| `settingsModule` | `:settings:settingsApp` | Settings feature bindings |

## Navigation

Three top-level destinations driven by `NavHost`:

| Route | Screen | Provider |
|---|---|---|
| `today` | Today note feed | `TodayScreenProvider` |
| `entries` | Past entries grouped by day | `EntriesScreenProvider` |
| `settings` | App settings | `SettingsScreenProvider` |

Navigation uses `launchSingleTop = true` and `restoreState = true` so re-selecting an active tab does not duplicate the back stack.

## Startup sequence

```
MeFirstApplication.onCreate()
 ├── startKoin(databaseModule, repositoryModule, todayModule, entriesModule, settingsModule)
 └── AlarmScheduler.schedule(flushHour, flushMinute)

MainActivity.onCreate()
 ├── Handle ACTION_SEND share intent
 ├── Show OnboardingScreen  (if not complete)
 ├── Show PinScreen         (if PIN is set)
 └── Show MainScreen        (normal flow)
```
