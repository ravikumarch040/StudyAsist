# StudyAsist

**The complete AI-powered study companion for students.**

Scan your textbooks, build a Q&A bank, practice with flashcards, track progress with spaced repetition, and ace your exams — all in one app.

**Scan -> Practice -> Track -> Improve**

Built with Kotlin + Jetpack Compose + Material 3.

## Build

- **Android Studio:** Open the project and run **Run > Run 'app'** (or build **Build > Make Project**).
- **Command line:** Use the included Gradle wrapper:
  ```bash
  ./gradlew assembleDebug   # Linux/macOS
  gradlew.bat assembleDebug # Windows
  ```
  Requires Java 17 or newer.

## Test

- **Unit tests:** `./gradlew :app:testDebugUnitTest` (or `gradlew.bat` on Windows)
- **Android instrumentation:** `./gradlew :app:connectedDebugAndroidTest` (requires device/emulator)

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

- **Language:** In-app language selector (System default, English, Hindi, Spanish, French, German). Overrides device language for the app.
- Default lead time, vibration, user name, TTS voice.
- **Backup/Restore:** JSON export and import via system share/save.
- **Cloud backup:** Pick folder (Drive, Dropbox, etc.) or use **direct Google Drive** (Sign in with Google); manual or daily auto backup; readable filenames (`StudyAsist_Backup_YYYY-MM-DD_HHmmss.json`); last backup time in Settings; completion/failure notifications with error details.
- Gemini API key for AI features.

### Export & Print

- **Export:** CSV, PDF, Excel from Timetable detail and Results; share via system sheet.
- **Share as image:** Timetable and assessment results rendered as PNG (from PDF) and shared via system sheet.
- **Print:** PDF of timetable and assessment results via system Print dialog (PrintManager).

### Gamification

- **Study streak:** Consecutive days with completed assessments.
- **Badges:** First Step, Week Warrior, Monthly Master, Perfect, etc.

### Study tools (Phase 3)

- **Change Voice:** Settings option to choose TTS voice for alarms and reading aloud.
- **Dictate:** Capture/upload image → OCR (ML Kit) → read aloud in chosen language.
- **Explain:** Text or image input → AI explanation (Gemini).
- **Solve:** Problem as text or image → AI step-by-step solution.

### Exam Goal

- **Goals & Q&A bank:** Create exam goals with subjects/chapters; scan pages via OCR or AI to build a question bank (MCQ, short, numeric, true/false, essay, etc.).
- **Assessments:** Create practice tests by goal, subject/chapter, or manual selection; timed runs; randomized questions.
- **Grading:** Objective grading with token-overlap scoring; numeric answers with tolerance check and Gemini-powered math equivalence (e.g. "1/2" vs "0.5"); LLM subjective grading for essays.
- **Voice & image answers:** SpeechRecognizer for voice input; camera/gallery with crop-region OCR; URIs persisted for audit.
- **Dashboard:** Track prediction (on-track/behind with hours/day metrics), score trend sparkline, activity heatmap, suggested practice areas, personal bests, "last practiced X days ago" per subject/chapter.
- **Notifications:** Exam goal alerts when exam is approaching and coverage is low; deep link directly to goal detail screen.
- **Manual review:** Flag answers, override scores, provide feedback.
- **Results:** Sort (newest, oldest, best, lowest), filter by assessment; export as PDF/CSV/Excel/image.

### Localization

- Full translations for **English** (base), **Hindi**, **Spanish**, **French**, and **German** (457 strings each).

### Spaced Repetition & Flashcards

- **SM-2 algorithm:** Adaptive scheduling based on your performance; review cards at optimal intervals.
- **Daily review:** Card-based review session with Again/Hard/Good/Easy ratings.
- **Flashcards:** Tinder-style swipe interface with card flip animation; swipe right for correct, left for wrong.
- **SRS integration:** All Q&A cards are automatically scheduled for review.

### Pomodoro Timer

- **Focus timer:** Configurable focus/break durations with animated circular countdown.
- **Session tracking:** Logs completed sessions to database; shows daily total focus time.
- **Auto-cycling:** Focus -> Short Break -> Focus -> Short Break -> Long Break.

### AI Features

- **AI study plans:** Gemini-powered personalized daily study plan generation based on goals and performance.
- **AI tutor chat:** Conversational AI tutor with context-aware responses and chat history.
- **PDF import:** Extract Q&A from PDF documents via PdfRenderer + OCR.

### Widgets

- **Schedule widget:** Today's upcoming activities at a glance.
- **Streak widget:** Study streak counter.
- **Quick review widget:** Due card count with one-tap review launch.
- **Countdown widget:** Days until next exam.

### UI Enhancements

- **Bottom navigation:** 5-tab layout (Home, Timetable, Study, Goals, More).
- **Dynamic theming:** Material You wallpaper-based colors on Android 12+.
- **11 themes:** Including Glassmorphism, High Contrast, and Dynamic.
- **Onboarding:** 4-page guided introduction for new users.
- **Confetti & animations:** Celebration effects for milestones.
- **Color psychology:** Activity types use research-backed color associations.
- **Accessibility:** Font scaling, haptic feedback, high contrast, color-blind mode.

### Market Features

- **Shareable assessments:** Generate challenge codes to share with friends.
- **Smart notifications:** Forgetting curve alerts and motivational nudges.
- **Timetable templates:** Pre-built schedules for common use cases.
- **Freemium-ready:** License architecture prepared for future monetization.

### Play Store Keywords

`study planner` `exam prep` `AI tutor` `flashcard` `spaced repetition` `focus timer` `pomodoro` `Q&A bank` `OCR study` `assessment practice`

## Planned / Future

- **Wear OS companion:** Pomodoro timer, flashcard review, streak complication.
- **Leaderboards:** Optional cloud leaderboards for score comparison.
- **Integration tests:** Full end-to-end suite covering scan → parse → assess → grade → result flows.
- **Offline AI:** On-device Gemini Nano for basic operations without internet.

---

See [docs/DESIGN.md](docs/DESIGN.md) for full design, [docs/SETUP-DRIVE-API.md](docs/SETUP-DRIVE-API.md) for Google Drive direct backup setup, [docs/PLAN-EXAM-GOAL.md](docs/PLAN-EXAM-GOAL.md) for exam goal feature.
