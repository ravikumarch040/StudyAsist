# StudyAsist – Design & Plan

**Version:** 1.1  
**Target:** Android (personal, single-user)  
**Scope:** Phase 1–3 + Exam Goal (implemented); Phase 2 extended.

---

## Implementation Status (as of v1.1)

The following are **implemented** beyond the original Phase 1 scope:

- **Export & Print:** CSV, PDF, Excel from Timetable detail and **Results**; Print via PrintManager for both.
- **Backup/Restore:** JSON export/import via system share/save in Settings.
- **Cloud backup:** User-selectable folder (Drive, Dropbox, etc.); manual or daily auto backup; `StudyAsist_Backup_YYYY-MM-DD_HHmmss.json` naming; last backup timestamp in Settings; completion/failure notifications with error details.
- **Color per type:** Activities use distinct colors by type (STUDY, BREAK, etc.) in timetable and home.
- **Simple analytics:** Weekly total minutes by activity type in Timetable detail.
- **Gamification:** Study streak, badges (First Step, Week Warrior, etc.).
- **Focus guard:** During STUDY blocks, alerts when user opens games/YouTube/social apps (Usage access); built-in + custom restricted package list; nudges via notification only (no blocking).
- **Study tools (Phase 3):** Dictate (OCR → TTS), Explain, Solve (Gemini); TTS voice selection in Settings.
- **Exam goal:** Goals, Q&A bank, assessments, attempts, results, manual review, export.

---

## 1. Tech Stack Recommendation

| Layer | Technology | Rationale |
|-------|------------|-----------|
| **Language** | Kotlin | First-class Android support, null-safety, coroutines. |
| **Min SDK** | 24 | Covers ~95%+ devices; Room, WorkManager, NotificationChannel work well. |
| **Target SDK** | 34 (or latest) | Play Store requirement; modern APIs. |
| **UI** | Jetpack Compose | Single UI toolkit, no XML layouts; faster iteration, Material 3. |
| **Architecture** | MVVM | Clear separation: UI ↔ ViewModel ↔ Repository ↔ Data. |
| **Local DB** | Room (SQLite) | Type-safe, migrations, perfect for timetables + activities. |
| **Async / Background** | Kotlin Coroutines + Flow | Reactive UI updates; suspend for DB/export. |
| **Notifications** | AlarmManager (exact) + WorkManager (fallback) | Exact alarms for on-time reminders; WorkManager for deferred/summary if needed. |
| **Export Excel** | Apache POI (poi-ooxml) or **JExcel** / **AndroidX no dependency** | Prefer **Apache POI** for .xlsx; add dependency only in app module. |
| **PDF / Print** | Android PrintManager + PdfDocument | Built-in; no extra lib for “print to PDF”. |
| **DI** | Hilt | Lightweight, standard for Android; scopes for Application, Activity, ViewModel. |
| **Navigation** | Compose Navigation | Single-Activity; type-safe routes optional (Kotlin serialization). |
| **Preferences** | DataStore (Preferences) | Replace SharedPreferences; type-safe, coroutine-friendly for Settings. |

**Alternatives considered**

- **Excel:** JExcel (lighter) if POI is too heavy; for “simple grid” export POI is reliable.
- **Print:** PrintManager + PdfDocument is sufficient for “print timetable”; no need for full PDF library.
- **Login:** None in Phase 1; optional “guest” or “local profile” name only.

---

## 2. High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        UI (Compose)                               │
│  TimetableList | TimetableDetail (Day/Week) | ActivityEdit |      │
│  Settings | Export/Share/Print dialogs                            │
└────────────────────────────┬────────────────────────────────────┘
                              │
┌────────────────────────────▼────────────────────────────────────┐
│                     ViewModels (MVVM)                             │
│  TimetableListVM | TimetableDetailVM | ActivityEditVM |           │
│  SettingsVM | ExportVM (one-off or embedded)                      │
└────────────────────────────┬────────────────────────────────────┘
                              │
┌────────────────────────────▼────────────────────────────────────┐
│  Repositories                                                     │
│  TimetableRepository | ActivityRepository | SettingsRepository   │
│  NotificationScheduler | ExportRepository                        │
└──────┬─────────────────────┬─────────────────────┬───────────────┘
       │                     │                     │
       ▼                     ▼                     ▼
┌──────────────┐    ┌─────────────────┐    ┌──────────────────────┐
│ Room (SQLite)│    │ DataStore       │    │ AlarmManager /       │
│ Timetable    │    │ (Settings)      │    │ NotificationManager  │
│ Activity     │    │                 │    │ WorkManager (opt)    │
└──────────────┘    └─────────────────┘    └──────────────────────┘
       │
       ▼
┌──────────────────────────────────────────────────────────────────┐
│  Export: FileProvider + Apache POI (.xlsx) → Share / Print (PDF)   │
└──────────────────────────────────────────────────────────────────┘
```

- **Offline-first:** All data in Room + DataStore; no network layer in Phase 1.
- **Single source of truth:** Repositories expose `Flow<List<T>>` for reactive UI.

---

## 3. Data Model (Detailed)

### 3.1 Entities (Room)

**Timetable**

| Column | Type | Notes |
|--------|------|--------|
| id | Long (PK, autoGenerate) | |
| name | String | e.g. "ICSE Class 5 Exam Timetable" |
| weekType | Enum (MON_SUN \| MON_SAT_PLUS_SUNDAY) | Mon–Sun single grid vs Mon–Sat + separate Sunday |
| startDate | Long? (epoch millis) | Optional; for “valid from” display only |
| createdAt | Long | For ordering / “last used” |
| updatedAt | Long | For conflict/backup later |

**Activity**

| Column | Type | Notes |
|--------|------|--------|
| id | Long (PK) | |
| timetableId | Long (FK) | |
| dayOfWeek | Int | 1=Monday … 7=Sunday (Calendar convention) |
| startTimeMinutes | Int | Minutes from midnight (e.g. 6:00 → 360) |
| endTimeMinutes | Int | Minutes from midnight |
| title | String | "Maths", "Lunch", "Tuition" |
| type | Enum | STUDY, BREAK, SCHOOL, TUITION, SLEEP |
| note | String? | Chapter, topic |
| notifyEnabled | Boolean | |
| notifyLeadMinutes | Int | 0, 5, 10 (override or use default from Settings) |
| sortOrder | Int | For same-slot ordering in UI |

**Indexes:** `(timetableId, dayOfWeek)`, `(timetableId, dayOfWeek, startTimeMinutes)` for overlap checks and day/week queries.

### 3.2 Enums

- **WeekType:** `MON_SUN`, `MON_SAT_PLUS_SUNDAY`
- **ActivityType:** `STUDY`, `BREAK`, `SCHOOL`, `TUITION`, `SLEEP`

### 3.3 Settings (DataStore)

- `defaultLeadMinutes`: Int (0, 5, 10)
- `vibrationEnabled`: Boolean
- `userName`: String (for TTS: “Hey {name}...”)
- `ttsVoiceName`: String? (TTS voice for alarms/reading)
- `geminiApiKey`: String (for Explain/Solve)
- `focusGuardEnabled`: Boolean
- `focusGuardRestrictedExtra`: String (comma-separated, user-added package names)
- `blockOverlap`: Boolean
- `cloudBackupFolderUri`: String? (DocumentsProvider tree URI)
- `cloudBackupAuto`: Boolean (daily periodic backup)
- `cloudBackupLastSuccessMillis`: Long (last successful backup timestamp)

### 3.4 DAOs (conceptual)

- **TimetableDao:** insert, update, delete, getAll(), getById(id), getByIdFlow(id)
- **ActivityDao:** insert, update, delete, getByTimetableId(id), getByTimetableAndDay(timetableId, day), getAllForTimetableFlow(timetableId)
- Overlap query: `getActivitiesInRange(timetableId, dayOfWeek, start, end)` excluding given activityId for edit case.

---

## 4. Screens & Navigation

### 4.1 Screen List

| Screen | Purpose |
|--------|--------|
| **TimetableList** | List all timetables; FAB “Add”; swipe/action to duplicate, delete, export. |
| **TimetableDetail** | Day view | Week view; filter by type; weekly analytics; Export CSV/PDF/Excel, Print, Share, Duplicate. |
| **ActivityEdit** | Add/Edit activity form (day, start/end time, title, type, note, notification toggle, lead time). Overlap warning at save. |
| **Settings** | Lead time, vibration, user name, TTS voice, focus guard, Backup/Restore, Cloud backup, block overlap, Gemini API key. |
| **ResultList** | Assessment results; Export CSV/PDF/Excel, Print. |
| **Study tools** | Dictate, Explain, Solve (OCR, AI). |
| **Exam goal** | Goals, Q&A bank, assessments, attempts, manual review. |
| **Dialogs** | Delete confirm; Overlap warning; Export success → Share. |

### 4.2 Navigation Graph (Compose)

- **NavHost** routes:  
  `timetable_list` → `timetable_detail/{id}?day={optional}` → `activity_edit/{timetableId}?activityId={optional}`  
  `settings`

- **Back stack:** List → Detail → ActivityEdit; Settings can be modal or separate.

### 4.3 Day View vs Week View

- **Day view:** Single day (horizontal timeline or vertical list of time slots). Time slots from min(startTime) to max(endTime) across all activities for that day; empty slots allowed.
- **Week view:** 7 (or 8 for Mon–Sat + Sunday) columns; rows = time slots (e.g. 30 min). Cell = activity name; optional type color strip.
- **Filter:** Dropdown or chips: All, Study only, Exams, etc. (filter by `ActivityType` or future tag).

---

## 5. Core Flows

### 5.1 Create Timetable

1. From TimetableList, FAB → “New timetable”.
2. Dialog or small form: Name, Week type (Mon–Sun / Mon–Sat + Sunday), optional Start date.
3. Save → insert Timetable → navigate to TimetableDetail (empty week/day view).
4. User adds activities from FAB or empty-state CTA.

### 5.2 Add/Edit Activity

1. From Detail (day or week), tap slot or FAB “Add activity” (with day preselected if from day view).
2. ActivityEdit: Day, Start time, End time, Title, Type, Note, Notify on/off, Lead minutes.
3. On Save: validate overlap (query existing in same day + time range). If overlap → show warning dialog; allow “Save anyway” or “Cancel”.
4. Save → insert/update Activity; if notifyEnabled, schedule/cancel notification (AlarmManager).

### 5.3 Duplicate Timetable

1. From List or Detail overflow: “Duplicate”.
2. Copy Timetable row (new id, name “{Original} (Copy)” or user-editable), then copy all Activities with new timetableId.
3. Optionally reschedule notifications for the new timetable.

### 5.4 Export to Excel

1. User taps “Export to Excel” in TimetableDetail.
2. Build grid: Row 0 = Time slots (e.g. 6:00, 6:30, …); Columns = Monday … Sunday (or + “Sunday” separate if week type).
3. Fill cells with activity title (and optionally type for color). Use Apache POI: create workbook, sheet name = timetable name (sanitized), write to app-specific directory (e.g. `getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)` or subfolder “StudyAsist”).
4. File path: `StudyAsist_{timetableName}_{timestamp}.xlsx`.
5. After write: trigger Android Share sheet (Intent.ACTION_SEND, FileProvider URI) so user can send via WhatsApp, email, etc.
6. Permissions: Scoped storage (no WRITE_EXTERNAL_STORAGE for app dir); if targeting older APIs, request as needed.

### 5.5 Print (PDF)

1. User taps “Print” in TimetableDetail.
2. Build same logical grid as week view (or day view); render to Canvas or use Compose’s `@Composable` to draw into PdfDocument (via Android’s PrintedPdfDocument or custom draw).
3. Use `PrintManager.print(...)` with a `PrintDocumentAdapter` that writes the generated PDF.
4. User chooses printer or “Save as PDF” in system dialog. No separate “PDF library” required if we use system print.

### 5.6 Share

- **Primary:** After Export Excel → Share sheet (share the .xlsx file).
- **Optional:** “Share as image” (screenshot of week view) using Compose’s bitmap capture; then share image. Phase 1 can be Excel-only; image share as Phase 2.

---

## 6. Notifications & Reminders

### 6.1 Scheduling Model

- For each **Activity** with `notifyEnabled == true`:
  - **Alarm time:** `activityStartTime - notifyLeadMinutes` (or default from Settings).
  - **Repeat:** Weekly (same dayOfWeek) for recurring timetable.
- Use **AlarmManager.setExactAndAllowWhileIdle()** (or setAlarmClock for visibility) for exact firing; handle Do Not Disturb by not overriding system (channel importance can be set to respect DND).
- Store **pending intent** identity: e.g. `activityId` in intent extra; on boot or app install-update, **reschedule all** from Room (no persistent alarm store needed beyond DB).

### 6.2 Notification Content

- **Channel:** One default channel “Study Reminder” (or per-timetable channel optional Phase 2).
- **Title:** Timetable name or “Study Reminder”.
- **Body:** “6:00–7:00 – Maths (Chapter: Fractions)”.
- **Tap:** Open app to TimetableDetail with that timetable and day focused; optional highlight activity (e.g. scroll to slot).

### 6.3 Sound & Vibration

- **NotificationChannel:** setSound(soundUri, audioAttributes), setVibrationPattern(long[]), setImportance.
- **User toggles:** Sound on/off, Vibration on/off in Settings; update channel and use in NotificationCompat.

### 6.4 Boot / Reinstall

- **BOOT_COMPLETED** receiver: Reschedule all alarms from Activity table (notifyEnabled = true). Use WorkManager one-time “reschedule notifications” worker if AlarmManager is not yet allowed post-boot.

### 6.5 Focus Guard (Study blocks)

- For **STUDY** activities with notifications and `focusGuardEnabled`: foreground service starts at block begin, ends at block end.
- Uses `UsageStatsManager` (requires Usage access); polls every 5s for foreground app.
- If app in restricted set (built-in + user custom): show alert notification (2 min cooldown per package).
- Does not block; nudges only.

---

## 7. Non-Functional Requirements

- **Performance:** Week view load &lt; 1 s: use Flow from Room, paging not required for one week; limit activities per timetable to e.g. 50 per day × 7 = 350 (still trivial for SQLite).
- **Min SDK 24, Target 34:** Use version checks for NotificationChannel (API 26+), exact alarms (API 31+), and export paths (scoped storage).
- **Offline-first:** No network; all data local.

---

## 8. Phase 2 (Status)

**Done:** Color per type, simple analytics (weekly minutes by type), Backup/Restore JSON, Cloud backup (DocumentsProvider).

**Done:** Share as image (timetable PDF rendered to PNG, shared via system sheet).

**Future:** Optional native Google Drive API.

---

## 9. File & Folder Structure (Suggested)

```
app/
  src/main/
    java/.../studyasist/
      data/
        local/
          dao/
          entity/
          db/
        repository/
        datastore/
      di/
      ui/
        timetablelist/
        timetabledetail/
        activityedit/
        settings/
        components/
      notification/
      export/
      util/
  build.gradle.kts
docs/
  DESIGN.md
```

---

## 10. Implementation Order (After Design Sign-off)

1. **Project setup:** Kotlin, Compose, Hilt, Room, DataStore; minSdk 24, target 34.
2. **Data layer:** Entities, DAOs, DB, migrations; DataStore for Settings.
3. **Repositories:** Timetable, Activity, Settings.
4. **UI – Timetable list:** List screen + FAB, create timetable (dialog/screen).
5. **UI – Timetable detail:** Day view + Week view tabs, empty state, navigate to add activity.
6. **UI – Activity add/edit:** Form, validation, overlap check.
7. **Notifications:** Channel, scheduling with AlarmManager, boot receiver.
8. **Export Excel:** POI, file in app dir, Share intent.
9. **Print:** PdfDocument + PrintManager.
10. **Duplicate timetable:** Copy entities + optional reschedule.
11. **Settings screen:** Lead time, sound, vibration.
12. **Polish:** Filter by type, icons, accessibility, strings.

---

## 11. Design Decisions to Finalize

| Topic | Options | Recommendation |
|-------|--------|----------------|
| **Week type “Mon–Sat + Sunday”** | Separate Sunday column vs same 7-day grid with Sunday last | Same 7-column grid; “Mon–Sat + Sunday” only changes label/order (e.g. “Sunday” as 7th column). |
| **Overlap** | Block vs warn | **Warn** and allow save (user may want “Lunch” overlapping two slots for display). |
| **Default week type** | Mon–Sun | Mon–Sun. |
| **Export format** | .xlsx only vs .xls fallback | .xlsx only (POI); .xls only if we need no POI and use CSV instead for “Excel-compatible”). |
| **Print** | In-app PDF only vs also “Open in Excel and print” | In-app PDF via PrintManager; “Export then print” documented as Option 1 in your spec. |

Once you confirm tech stack (Kotlin, Compose, Room, Hilt, POI, AlarmManager) and the decisions above, implementation can follow this plan step by step.
