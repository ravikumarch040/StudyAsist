# StudyAsist

Personal study timetable app for Android (Kotlin + Jetpack Compose).

## Build

- **Android Studio:** Open the project and run **Run > Run 'app'** (or build **Build > Make Project**).
- **Command line:** Ensure you have the Gradle wrapper (e.g. let Android Studio sync once, or run `gradle wrapper` if Gradle is installed). Then:
  ```bash
  ./gradlew assembleDebug   # Linux/macOS
  gradlew.bat assembleDebug # Windows
  ```

## Implemented

- **Home:** Today tab (active timetable, today’s activities, current-activity highlight) and Timetables tab.
- **Active timetable:** Set active from Timetables list; only the active timetable drives Today and notifications.
- **Timetables:** Create, duplicate, delete; open detail; set active.
- **Timetable detail:** Day view and **Week view** (7-column grid); **filter by type** (All, Study, Break, etc.); add/edit activity.
- **Activity add/edit:** Day, start/end (number pickers), title, type, note, notification; default start 5:00 or last activity’s end; copy schedule from another day; overlap warning.
- **Notifications:** Channel “Study Reminder”; AlarmManager for activities with notify on (active timetable only); BootReceiver reschedules via WorkManager after boot; runtime POST_NOTIFICATIONS for API 33+.
- **Settings:** Default lead time, sound, vibration.
- **Polish:** String resources for main labels and empty states; content descriptions for key actions.

## Phase 2 (to do)

- **Export:** CSV/Excel to app storage and Share sheet.
- **Print:** PDF of timetable and system Print dialog (PrintManager).
- Optional: color per type, simple analytics, backup/restore.

See [docs/DESIGN.md](docs/DESIGN.md) for full design and data model.
