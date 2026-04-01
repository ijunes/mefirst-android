# :entries

The Entries feature displays past journal notes grouped by day. It mirrors the three-module structure used by `:today` to enforce the same dependency boundaries.

## Module Structure

```
:entries              (API)   — interfaces, abstract classes, UI models
:entries:entriesImpl  (impl)  — concrete implementations
:entries:entriesApp   (wiring) — Koin bindings, depends on both above
```

### :entries (API module)

Public contracts consumed by `:app`. Contains no implementation logic.

| Type | Class | Purpose |
|---|---|---|
| Abstract ViewModel | `EntriesViewModel` | Exposes `entries: StateFlow<Map<Long, List<Message>>>` — past notes keyed by day (epoch-ms start-of-day) |
| Screen provider interface | `EntriesScreenProvider` | Composable contract for rendering the Entries screen |
| UI model | `EntriesScreenUiModel` | Wraps `entries: Map<Long, List<Message>>` passed into the screen |
| Repository interfaces | `EntriesRepository`, `WorkEntriesRepository` | Data access contracts for personal and work-mode past entries |

#### Repository methods

Both `EntriesRepository` and `WorkEntriesRepository` expose:

| Method | Description |
|---|---|
| `getAllEntries(): Flow<Map<Long, List<*>>>` | All entries grouped by day; emits on every change |
| `getAllEntriesByDate(startOfDay, endOfDay): Flow<List<*>>` | Entries within a specific day's epoch-ms range |

### :entries:entriesImpl (implementation module)

Concrete implementations. `:app` never depends on this module directly.

| Type | Class | Purpose |
|---|---|---|
| ViewModel | `EntriesScreenViewModelImpl` | Implements `EntriesViewModel`; switches between personal and work repositories based on `ModeStateHolder.isWorkMode` |
| Screen | `EntriesScreen` | Composable that renders the date-grouped entry feed |
| Screen provider | `EntriesScreenProviderImpl` | Implements `EntriesScreenProvider` by delegating to `EntriesScreen` |
| Repositories | `EntriesRepositoryImpl`, `WorkEntriesRepositoryImpl` | Implement the repository interfaces against the Room database |

### :entries:entriesApp (wiring module)

Owns all Koin registrations for the feature. The only module that imports from both `:entries` and `:entries:entriesImpl`.

```kotlin
val entriesModule = module {
    factory<EntriesRepository>      { EntriesRepositoryImpl(get()) }
    factory<WorkEntriesRepository>  { WorkEntriesRepositoryImpl(get()) }
    viewModel<EntriesViewModel>     { EntriesScreenViewModelImpl(get(), get(), get()) }
    single<EntriesScreenProvider>   { EntriesScreenProviderImpl() }
}
```

## Dependency Graph

```
:app
 ├── :entries              (types only — EntriesViewModel, EntriesScreenProvider)
 └── :entries:entriesApp   (Koin module — entriesModule)

:entries:entriesApp
 ├── :entries
 └── :entries:entriesImpl

:entries:entriesImpl
 └── :entries
```

## Data Flow

```
MainActivity
 │  injects EntriesViewModel (Koin) ──► EntriesScreenViewModelImpl
 │  injects EntriesScreenProvider (Koin) ──► EntriesScreenProviderImpl
 │
 └── MainScreen(entriesScreenProvider, uiState)
      └── entriesScreenProvider.Content(EntriesScreenUiModel(entries))
           └── EntriesScreen (from :entriesImpl)
```

`EntriesViewModel.entries` is a `Map<Long, List<Message>>` where each key is the epoch-ms start of a day, and the value is the ordered list of notes for that day. The ViewModel switches its upstream source automatically when work mode changes via `ModeStateHolder`.
