# StudyAsist

Personal study timetable app for Android (Kotlin + Jetpack Compose).

## Build

- **Android Studio:** Open the project and run **Run > Run 'app'** (or build **Build > Make Project**).
- **Command line:** Ensure you have the Gradle wrapper (e.g. let Android Studio sync once, or run `gradle wrapper` if Gradle is installed). Then:
  ```bash
  ./gradlew assembleDebug   # Linux/macOS
  gradlew.bat assembleDebug # Windows
  ```

## Implemented (Phase 1)

- **Timetable list:** Create, duplicate, delete timetables; week type (Mon–Sun / Mon–Sat + Sunday).
- **Timetable detail:** Day view with day selector (Mon–Sun); list of activities per day; add/edit activity.
- **Activity add/edit:** Day, start/end time, title, type (Study, Break, School, Tuition, Sleep), note, notification toggle and lead time; overlap warning (warn and allow save anyway).
- **Settings:** Default notification lead time (0/5/10 min), sound and vibration toggles.
- **Data:** Room (Timetable + Activity), DataStore (settings), MVVM + Hilt.

## To do (next)

- Notifications: channel, AlarmManager scheduling, BootReceiver reschedule.
- Export: CSV/Excel to app storage and Share sheet.
- Print: PDF generation and PrintManager.
- Week view (7-column grid) and filter by activity type.

See [docs/DESIGN.md](docs/DESIGN.md) for full design and data model.
