# Plan: Finish All Pending Features

**Created:** 2025-02  
**Goal:** Implement all identified pending features in a single coordinated effort.

---

## Summary

| Phase | Feature | Effort | Dependencies |
|-------|---------|--------|--------------|
| 1 | Accessibility Settings UI | Small | SettingsViewModel additions |
| 1 | Pomodoro Settings UI | Small | SettingsViewModel additions |
| 2 | PDF Import for Q&A Bank | Medium | PdfExtractor (exists), QAScan flow |
| 3 | "Ask about this question" → AI Tutor | Small | Nav with args, TutorChatViewModel |
| 4 | Credential Manager migration | Medium | Google APIs, Drive backup refactor |
| 5 | Wear OS Streak sync | Medium | Wearable Data Layer |
| 6 | Offline AI (Gemini Nano) | Large | AICore SDK, device-dependent |
| 7 | iOS app | Separate | Swift/SwiftUI, backend sync |

**Recommended order for this sprint:** 1 → 2 → 3 (Phases 1–3 are self-contained and high-value).

---

## Phase 1: Settings UI Additions (2–3 days)

### 1.1 Accessibility Section

**Backend:** Already in `SettingsRepository`:
- `fontScaleFlow`, `setFontScale` (0.85, 1.0, 1.15, 1.3)
- `hapticEnabledFlow`, `setHapticEnabled`
- `highContrastModeFlow`, `setHighContrastMode`
- `colorBlindModeFlow`, `setColorBlindMode`

**Tasks:**

1. **SettingsViewModel** – Add state flows and setters:
   - `fontScale`, `setFontScale`
   - `hapticEnabled`, `setHapticEnabled`
   - `highContrastMode`, `setHighContrastMode`
   - `colorBlindMode`, `setColorBlindMode`

2. **SettingsScreen** – Add section `settings_section_accessibility`:
   - Font scale: FilterChips or slider (Small, Medium, Large, Extra Large)
   - Haptic feedback: Switch
   - High contrast: Switch
   - Color-blind friendly: Switch

3. **Apply font scale** – `MainViewModel` has `fontScaleFlow`; wire to `LocalDensity` or `FontScaleConfiguration` at app root so Compose respects it.

4. **Strings** – Use existing: `font_size`, `font_size_small`, `font_size_medium`, `font_size_large`, `font_size_extra_large`, `haptic_feedback`, `haptic_feedback_summary`, `high_contrast`, `high_contrast_summary`, `color_blind_mode`, `color_blind_summary`.

**Files:**
- `SettingsViewModel.kt`
- `SettingsScreen.kt`
- `MainActivity.kt` or `StudyAsistApp.kt` (font scale application)
- `strings.xml` (if any new strings needed)

---

### 1.2 Pomodoro Settings Section

**Backend:** Already in `SettingsRepository`:
- `pomodoroFocusMinutesFlow`, `setPomodoroFocusMinutes` (25, 45, 50)
- `pomodoroShortBreakMinutesFlow`, `setPomodoroShortBreakMinutes` (5, 10, 15)
- `pomodoroLongBreakMinutesFlow`, `setPomodoroLongBreakMinutes` (15, 20, 30)
- `pomodoroAutoStartBreaksFlow`, `setPomodoroAutoStartBreaks`

**Tasks:**

1. **SettingsViewModel** – Add:
   - `pomodoroFocusMinutes`, `setPomodoroFocusMinutes`
   - `pomodoroShortBreakMinutes`, `setPomodoroShortBreakMinutes`
   - `pomodoroLongBreakMinutes`, `setPomodoroLongBreakMinutes`
   - `pomodoroAutoStartBreaks`, `setPomodoroAutoStartBreaks`

2. **SettingsScreen** – Add section `settings_section_pomodoro`:
   - Focus duration: FilterChips (25, 45, 50 min)
   - Short break: FilterChips (5, 10, 15 min)
   - Long break: FilterChips (15, 20, 30 min)
   - Auto-start breaks: Switch

3. **PomodoroViewModel** – Already reads from `settingsRepository`; no change if Settings UI saves to DataStore.

**Strings:** `pomodoro_focus_duration`, `pomodoro_short_break_duration`, `pomodoro_long_break_duration`, `pomodoro_auto_start_breaks` (exist).

**Files:**
- `SettingsViewModel.kt`
- `SettingsScreen.kt`
- `strings.xml` (add `settings_section_pomodoro`)

---

## Phase 2: PDF Import for Q&A Bank (3–4 days)

### 2.1 Overview

- `PdfExtractor` exists: renders PDF pages to bitmaps.
- Q&A flow: OCR → parse → editable rows → save to bank.
- Add "Import PDF" entry to Q&A Bank screen; re-use QAScan pipeline for selected pages.

### 2.2 Tasks

1. **Route** – Add `NavRoutes.PDF_IMPORT = "pdf_import"` or handle as modal/sheet from Q&A Bank.

2. **PdfImportScreen** (new):
   - `ActivityResultContracts.OpenDocument()` for PDF.
   - Use `PdfExtractor.getPageCount()` and `extractPages()`.
   - Show page thumbnails with checkboxes (select which pages to process).
   - Progress: "Processing page X of Y" (OCR per page).
   - On finish: navigate to QAScan with pre-filled rows (or direct save to bank).

3. **QAScanViewModel** – Add `loadFromPdfPages(pages: List<PdfPage>)` or equivalent:
   - For each page bitmap: run OCR (OcrHelper), optional AI improve.
   - Parse to Q&A rows (heuristic + optional Gemini).
   - Populate `uiState` rows; user can edit before Save.

4. **Entry point** – QABankScreen: FAB or overflow menu item "Import PDF" → open PDF picker → PdfImportScreen.

5. **Strings** – `import_pdf` exists; add `processing_page_format`, `select_pages_hint` if needed.

**Files:**
- `NavRoutes.kt`
- `PdfImportScreen.kt` (new)
- `PdfImportViewModel.kt` (new)
- `QAScanViewModel.kt` (extend)
- `QABankScreen.kt` (add Import PDF action)
- `AppNavGraph.kt` (composable for PDF import)
- `strings.xml`

---

## Phase 3: "Ask about this question" → AI Tutor (1–2 days)

### 3.1 Overview

- On Assessment Result, each question detail card gets a button "Ask about this question".
- Tapping navigates to Tutor Chat with pre-filled prompt including question text.
- Tutor responds in context of that question.

### 3.2 Tasks

1. **Route** – Extend: `TUTOR_CHAT` or add `TUTOR_CHAT_WITH_QUESTION = "tutor_chat?question={question}"`.
   - Simpler: use optional query arg: `tutor_chat?initialQuestion=...` (URL-encode).

2. **TutorChatViewModel** – Add `initialQuestion: String?`:
   - On init, if non-null: pre-insert user message and optionally auto-send or show in input field.
   - Or: set `inputText` state so user sees it and taps Send.

3. **TutorChatScreen** – Accept `initialQuestion: String?`; pass to ViewModel (via Hilt SavedStateHandle or constructor param from navigation).

4. **AssessmentResultScreen** – For each `uiState.details` item:
   - Add small button/icon "Ask about this" (use `ask_about_this` string).
   - `onClick`: `onAskAboutQuestion(item.questionText)` → navigate to Tutor with encoded question.

5. **AppNavGraph** – `composable(NavRoutes.TUTOR_CHAT)`:
   - Read `SavedStateHandle["initialQuestion"]` if using nav args.
   - Or: `composable("tutor_chat?initialQuestion={initialQuestion}")` with optional arg.

6. **AssessmentResultViewModel** – Expose `questionText` per detail (likely already in `uiState.details`).

**Files:**
- `NavRoutes.kt` (optional arg)
- `TutorChatViewModel.kt`
- `TutorChatScreen.kt`
- `AssessmentResultScreen.kt`
- `AppNavGraph.kt`

---

## Phase 4: Credential Manager Migration (3–5 days)

### 4.1 Overview

- Replace deprecated `GoogleSignIn` + `GoogleAccountCredential` with Credential Manager.
- See `docs/PLAN-CREDENTIAL-MANAGER.md`.

### 4.2 Tasks

1. Add Credential Manager dependencies (if not present).
2. `DriveApiBackupProvider`: use `CredentialManager.getCredential()` with `GoogleIdTokenRequest`.
3. `SettingsViewModel`: replace Google Sign-In flow for Drive with Credential Manager.
4. Test: backup to Drive, restore, sign out.
5. Remove `@Suppress("DEPRECATION")` from affected files.

**Files:**
- `DriveApiBackupProvider.kt`
- `SettingsViewModel.kt`
- `build.gradle.kts`

---

## Phase 5: Wear OS Streak Sync (2–3 days)

### 5.1 Overview

- `StreakScreen.kt` (Wear): currently hardcoded `streakDays = 0`.
- Use Wearable Data Layer to receive streak from phone.

### 5.2 Tasks

1. ~~Add Wearable Data Layer dependency to Wear module.~~ (play-services-wearable already present)
2. ~~Phone: `MainActivity` or a service: on streak update, send to `WearableClient` or `DataClient`.~~
3. ~~Wear: `StreakScreen` subscribes to data; display received streak.~~
4. ~~Fallback: show "Sync required" or 0 when no data.~~

**Implemented:**
- `WearSyncManager` (app module): syncs streak to Wear via PutDataMapRequest
- `HomeViewModel`, `AssessmentResultViewModel`: call `wearSyncManager.syncStreak()` when streak changes
- `StreakScreen` (wear): fetches from DataClient, adds listener for updates; shows "Sync with phone" when no data
- Wear app: Pomodoro screen has Streak button; StreakScreen shows synced value with Back

---

## Phase 6: Offline AI (Gemini Nano) – Implemented

### 6.1 Overview

- See `docs/PLAN-OFFLINE-AI.md`.
- Device-dependent (Pixel 8+, selected Samsung).
- Fallback when no network or no API key.

### 6.2 Implemented

- **OfflineGeminiProvider**: ML Kit GenAI Prompt API for on-device Gemini Nano
- **GeminiRepository.generateContentWithFallback**: Cloud first, then offline when cloud fails
- **ExplainViewModel** & **SolveViewModel**: Use `generateContentWithFallback` for explain/solve

---

## Phase 7: iOS App – Separate Project

- See `StudyAsist-iOS/README.md`.
- Planned separately; not part of Android sprint.

---

## Implementation Checklist (Phases 1–3)

- [x] **1.1** SettingsViewModel – accessibility state + setters
- [x] **1.1** SettingsScreen – Accessibility section
- [x] **1.1** App – apply font scale at root
- [x] **1.2** SettingsViewModel – Pomodoro state + setters
- [x] **1.2** SettingsScreen – Pomodoro section
- [x] **2.1** NavRoutes – PDF import route
- [x] **2.2** PdfImportScreen + ViewModel
- [x] **2.3** QAScanViewModel – loadFromPdfPages (via PendingPdfImportHolder)
- [x] **2.4** QABankScreen – Import PDF entry
- [x] **2.5** AppNavGraph – PDF import composable
- [x] **3.1** NavRoutes – TUTOR_CHAT with optional initialQuestion
- [x] **3.2** TutorChatViewModel – handle initialQuestion
- [x] **3.3** TutorChatScreen – initialQuestion param
- [x] **3.4** AssessmentResultScreen – "Ask about this" button
- [x] **3.5** AppNavGraph – pass initialQuestion to Tutor
- [x] **Doc** Update FEATURES_AND_USER_GUIDE (Accessibility, Pomodoro, PDF Import, Ask about this)
- [x] **Doc** Update TEST_CASES_SUITE (new test cases)

---

## Estimated Timeline (Phases 1–3)

| Phase | Days |
|-------|------|
| Phase 1 (Accessibility + Pomodoro) | 2 |
| Phase 2 (PDF Import) | 3 |
| Phase 3 (Ask about this) | 1 |
| **Total** | **~6 days** |

Phases 4–7 are separate efforts and can be scheduled later.
