# NoteShade

A polished offline-first Android note-taking app with persistent notification notes.

## Chosen stack
- **Tech stack:** Kotlin, Jetpack Compose, Room, DataStore, Navigation Compose, MVVM
- **Architecture:** single-module MVVM with repository layer and unidirectional-ish UI state
- **Storage:** local Room database for notes + DataStore Preferences for settings
- **Notification plan:** each note can be surfaced as its own low-importance ongoing notification with actions for open, archive, and hide; notifications are restored on boot/app update

## Features
- Create, edit, delete, search, pin, archive, unarchive notes
- Title + body notes with auto-save support
- Sorting: pinned, newest, oldest, recently updated
- Archived section in the main screen
- First-run guidance and empty states
- Settings for theme, default sort, notifications, auto-save
- JSON backup export via Android document picker
- Offline-first local storage
- Persistent note notifications in the Android shade

## Build
1. Install Android Studio Hedgehog+ or a recent command-line Android SDK.
2. Ensure JDK 17 and Android SDK platform 34/build-tools 34 are available.
3. Open the project or run:
   ```bash
   ./gradlew assembleDebug
   ```
4. APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

## Architecture brief
- `data/`: Room entities, DAO, repositories, settings store
- `viewmodel/`: notes list, note editor, settings state/actions
- `ui/`: Compose screens, navigation, theme
- `notifications/`: notification channel + ongoing note notifications
- `receivers/`: notification quick actions + boot restore

## Screenshot guidance
Capture:
1. Main list with pinned and archived sections visible
2. Editor with notification toggle enabled
3. Settings screen
4. Notification shade showing one or more persistent notes

## Caveats
- Backup export writes JSON only; import is not implemented yet.
- Notification permission is requested on Android 13+.
