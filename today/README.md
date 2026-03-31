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
| Abstract ViewModel | `TodayViewModel` | Exposes state flows and event handler to the host Activity |
| Screen provider interface | `TodayScreenProvider` | Composable contract for rendering the Today screen |
| UI model | `TodayScreenUiModel` | Data passed into the Today screen |
| Activity commands | `TodayAction` | Sealed interface for one-shot commands emitted to the Activity (record audio, launch gallery, launch camera) |
| Repository interfaces | `TodayRepository`, `WorkTodayRepository` | Data access contracts |

### :today:todayImpl (implementation module)

Concrete implementations. `:app` never depends on this module directly.

| Type | Class | Purpose |
|---|---|---|
| ViewModel | `TodayScreenViewModelImpl` | Implements `TodayViewModel`; handles recording, note insertion, mode switching |
| Screen | `TodayScreen` | Composable that renders the chat-style note feed |
| Screen provider | `TodayScreenProviderImpl` | Implements `TodayScreenProvider` by delegating to `TodayScreen` |
| Repositories | `TodayRepositoryImpl`, `WorkTodayRepositoryImpl` | Implement the repository interfaces against the Room database |

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

The Activity collects `TodayViewModel.activityCommands` to handle `TodayAction` events (permission requests, launcher launches) that cannot be performed inside a Composable.
