# :database

The single Room database for the app. All feature modules that need data access depend on `:database` for entity and DAO types; they never create their own database instances.

## Database

`MeFirstDatabase` — Room database at version **2**, registered as a Koin singleton in `:app`'s `databaseModule`.

| DAO accessor | DAO interface | Table |
|---|---|---|
| `todayDao()` | `TodayDao` | `today` |
| `workTodayDao()` | `WorkTodayDao` | `work_today` |
| `entriesDao()` | `EntriesDao` | `entries` |
| `workEntriesDao()` | `WorkEntriesDao` | `work_entries` |

## Entities

All entities use `timeStamp: Long` (epoch-ms) as their primary key.

| Entity | Table | Used by |
|---|---|---|
| `NoteEntity` | `today` | Personal today notes |
| `WorkTodayEntity` | `work_today` | Work-mode today notes |
| `EntryEntity` | `entries` | Personal past entries |
| `WorkEntryEntity` | `work_entries` | Work-mode past entries |

Each entity shares the same column structure:

| Column | Type | Description |
|---|---|---|
| `timeStamp` | `Long` (PK) | Creation time in epoch-ms |
| `note_text` / `entry_text` | `String?` | Text content, null for media notes |
| `media_type` | `MediaType` | `TEXT`, `VOICE`, or `IMAGE` (stored as ordinal `Int`) |
| `media_path` | `String?` | Absolute file path or content URI string for media |
| `waveform_path` | `String?` | File path of the PNG waveform image (voice notes only) |

## DAOs

### TodayDao / WorkTodayDao

| Method | Description |
|---|---|
| `getAll(): Flow<List<*>>` | Observe all rows; emits on every change |
| `insert(note)` | Insert a single note |
| `insertAll(vararg notes)` | Bulk insert |
| `delete(note)` | Delete a single note by primary key |
| `deleteAll()` | Delete all rows in the table |

### EntriesDao / WorkEntriesDao

| Method | Description |
|---|---|
| `getAllEntries(): Flow<List<*>>` | Observe all rows |
| `getEntriesByDate(startOfDay, endOfDay): Flow<List<*>>` | Observe rows within a day's epoch-ms range |
| `addAllNoteEntries(vararg entries)` | Bulk insert |
| `deleteAll()` | Delete all rows in the table |

## Type converters

`Converters` maps `MediaType` to its ordinal `Int` and back. An out-of-range ordinal falls back to `MediaType.TEXT`.

## Migrations

| Migration | Change |
|---|---|
| `MIGRATION_1_2` | Adds the `work_today` and `work_entries` tables to support work-mode data |

`fallbackToDestructiveMigration(false)` is set — missing migrations will throw rather than silently drop data.
