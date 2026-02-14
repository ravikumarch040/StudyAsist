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

### Core

- **Home:** Today tab (active timetable, today’s activities, current-activity highlight) and Timetables tab.
- **Active timetable:** Set active from Timetables list; only the active timetable drives Today and notifications.
- **Timetables:** Create, duplicate, delete; open detail; set active.
- **Timetable detail:** Day view and **Week view** (7-column grid); **filter by type** (All, Study, Break, etc.); add/edit activity; **simple analytics** (weekly total by activity type: e.g. Study 12h, Break 3h).
- **Activity add/edit:** Day, start/end (number pickers), title, type, note, notification; default start 5:00 or last activity’s end; copy schedule from another day; overlap warning.
- **Color per type:** Activities use distinct colors by type (STUDY, BREAK, SCHOOL, etc.) in timetable and home.

### Notifications

- **Reminders:** Channel “Study Reminder”; AlarmManager for activities with notify on (active timetable only); BootReceiver reschedules via WorkManager after boot; runtime POST_NOTIFICATIONS for API 33+.
- **Focus guard:** During Study blocks, alerts when you open games, YouTube, Instagram, or other chosen apps (requires Usage access). Does not block; nudges via notification.

### Settings

- Default lead time, vibration, user name, TTS voice.
- **Backup/Restore:** JSON export and import via system share/save.
- Gemini API key for AI features.

### Export & Print

- **Export:** CSV, PDF, Excel from Timetable detail and Results; share via system sheet.
- **Print:** PDF of timetable and assessment results via system Print dialog (PrintManager).

### Gamification

- **Study streak:** Consecutive days with completed assessments.
- **Badges:** First Step, Week Warrior, Monthly Master, Perfect, etc.

### Study tools (Phase 3)

- **Change Voice:** Settings option to choose TTS voice for alarms and reading aloud.
- **Dictate:** Capture/upload image → OCR (ML Kit) → read aloud in chosen language.
- **Explain:** Text or image input → AI explanation (Gemini).
- **Solve:** Problem as text or image → AI step-by-step solution.

### Exam goal

- Goals, Q&A bank, assessments, attempts, results, manual review, export.

## Planned / Future

- **Cloud backup:** Deeper Google Drive integration (scheduled backups already work with DocumentsProvider).

---

See [docs/DESIGN.md](docs/DESIGN.md) for full design and data model, [docs/PLAN-STUDY-TOOLS.md](docs/PLAN-STUDY-TOOLS.md) for study tools, [docs/PLAN-EXAM-GOAL.md](docs/PLAN-EXAM-GOAL.md) for exam goal feature.
