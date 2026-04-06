# NoteShade

Offline-first Android note-taking app with persistent note notifications.

## Tech stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Architecture:** single-activity app with layered data/UI separation
- **Persistence:** Room for notes, DataStore Preferences for settings
- **Build:** Gradle Kotlin DSL, Android Gradle Plugin 8.5, Kotlin 1.9
- **Min SDK / Target SDK:** 26 / 34

## App architecture

- `data/`
  - `NoteEntity`, `NoteDao`, `AppDatabase`
  - `NoteRepository` for note CRUD/toggles
  - `SettingsRepository` for onboarding/theme/sort preferences
- `ui/`
  - `home/` active + archived searchable note lists
  - `editor/` note editor with auto-save
  - `settings/` app preferences and onboarding reset
  - `components/` small shared helpers
- `navigation/`
  - Compose navigation host for home, editor, and settings
- `notifications/`
  - coordinator that observes notes marked for notification display and publishes ongoing expandable notifications with actions

## Storage approach

- **Notes:** stored locally in a Room SQLite database (`noteshade.db`)
- **Settings:** stored locally in DataStore Preferences
- **Offline-first:** all features work locally without network access; GitHub is only needed for publishing source

## Notification implementation plan

NoteShade lets users surface one or more notes as persistent notifications:

- each note can be toggled into notification mode
- notifications are **ongoing** and **expandable** (`BigTextStyle`)
- tapping a notification deep-opens the note editor
- quick actions currently include:
  - **Pin / Unpin**
  - **Archive**
  - **Hide** (remove from notification shade)
- notification state is driven by Room data, so changes in the app or action buttons stay in sync

## Feature summary

- create notes
- edit notes with auto-save
- delete notes with confirmation
- pin / unpin notes
- search notes by title or body
- sort by:
  - newest
  - oldest
  - pinned first
  - recently updated
- archive / unarchive notes
- searchable archived section
- settings page
- first-run guidance dialog
- empty states for active/archived/search cases
- robust local-only storage
- persistent multi-note notification core

## Build instructions

### Prerequisites

- JDK 17
- Android SDK platform 34 + build-tools 34.0.0

### Build debug APK

```bash
export JAVA_HOME=/path/to/jdk-17
export ANDROID_SDK_ROOT=/path/to/android-sdk
./gradlew assembleDebug
```

Generated APK:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Screenshot guidance

Recommended screenshots for README/store prep:

1. **Home / active notes** — show pinned and notification-enabled notes
2. **Editor** — show title/body editing and auto-save messaging
3. **Archived tab** — demonstrate searchable archive
4. **Settings** — show theme and sort preferences
5. **Notification shade** — expanded ongoing note notification with actions visible

Use an emulator or device with a few sample notes so the UI looks lived-in.

## Notes

This is a practical MVP intended to be maintainable and easy to extend. Natural next additions would be export/import, labels, markdown formatting, richer reminder scheduling, and tests.
