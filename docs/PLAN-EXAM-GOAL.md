# Exam Goal — Implementation Plan

This document maps the [Exam Goal feature design](#overview) to the StudyAsist codebase and breaks implementation into sprints with concrete tasks and file-level changes.

---

## 1. Overview & alignment with current app

| Design area | Current app | Integration approach |
|-------------|-------------|----------------------|
| **Client** | Kotlin, Jetpack Compose, Hilt, Navigation Compose | Add new screens/routes under `ui/`; new DAOs/entities in `data/local/`. |
| **Local DB** | Room (`AppDatabase`), 2 entities, migrations | Add 8 new entities + DAOs; bump DB version; add migrations. |
| **OCR** | `OcrHelper.kt` — ML Kit, `extractTextFromImage(context, Uri)` | Reuse for Q&A scan; no API change. Optional: extend for region/crop later. |
| **TTS / Language** | `TtsHelper`, `TtsVoiceHelper`, `LanguageOptions` | Reuse for “read question aloud” and any voice hints (Sprint 2+). |
| **Background** | WorkManager + Hilt (`RescheduleNotificationsWorker`, etc.) | Add workers for: Q&A import/parse, deferred cloud grading, export. |
| **Network** | Retrofit + OkHttp (e.g. GeminiRepository) | Add optional backend API for LLM parse, grading, CAS (Sprint 2+). |
| **Navigation** | `AppNavGraph`, `NavRoutes` | Add routes: goals list, goal detail/dashboard, goal edit, QA bank, scan/edit, assessment create, assessment run, results. |
| **Entry** | HomeScreen tabs (Today / Timetables / Study Tools) | Add “Exam Goal” as new tab or card under Study Tools linking to goal dashboard. |

---

## 2. Data model — implementation details

### 2.1 New entities and packages

Create under `com.studyasist.data.local.entity`:

| Entity | File | Notes |
|--------|------|--------|
| `Goal` | `Goal.kt` | `examDate: Long`, `isActive: Boolean`. |
| `GoalItem` | `GoalItem.kt` | `goalId: Long` (FK to Goal), `chapterList: String` (comma or JSON). |
| `QA` | `QA.kt` | `QuestionType` enum + converter; `optionsJson`, `metadataJson` nullable. |
| `Assessment` | `Assessment.kt` | `goalId` nullable. |
| `AssessmentQuestion` | `AssessmentQuestion.kt` | `assessmentId`, `qaId`, `weight`, `sequence`. |
| `Attempt` | `Attempt.kt` | `assessmentId`, `startedAt`, `endedAt` nullable. |
| `AttemptAnswer` | `AttemptAnswer.kt` | `attemptId`, `qaId`, `answerText` / `answerImageUri` / `answerVoiceUri`. |
| `Result` | `Result.kt` | `attemptId`, `score`, `maxScore`, `percent`, `detailsJson`. |

**QuestionType enum** (store in DB via TypeConverter):

```kotlin
enum class QuestionType { MCQ, FILL_BLANK, SHORT, ESSAY, NUMERIC, TRUE_FALSE, MATCHING, DIAGRAM }
```

- Add `QuestionTypeConverter` (same pattern as `ActivityTypeConverter`) and register in `AppDatabase` `@TypeConverters`.

**Optional:** `SourceCapture` entity if you want to store image references for “source crop” UX; design allows `sourceCaptureId: Long?` on QA pointing to “saved image/text”. For MVP, this can be a Long reference to a file path or content URI stored in metadata.

### 2.2 Foreign keys and indices

- `GoalItem`: `ForeignKey(entity = Goal::class, parentColumns = ["id"], childColumns = ["goalId"], onDelete = CASCADE)`.
- `AssessmentQuestion`: FK to `Assessment`, FK to `QA`.
- `Attempt`: FK to `Assessment`.
- `AttemptAnswer`: FK to `Attempt`, and logical reference to `qaId` (no FK to QA if QA can be deleted; or soft-delete QA).
- `Result`: FK to `Attempt`.

Indices: `goalId` on goal_items, assessments; `assessmentId` on assessment_questions, attempts; `attemptId` on attempt_answers, results; `subject`, `chapter` on qa_bank for filters.

### 2.3 Database migration

- Current version: **2**. New version: **3**.
- Migration 2→3: create all 8 new tables (goals, goal_items, qa_bank, assessments, assessment_questions, attempts, attempt_answers, results).
- Add `MIGRATION_2_3` in `AppDatabase.kt` and include in `migrations()`; add new abstract DAO methods and `@Database(entities = [..., Goal::class, ...], version = 3)`.

---

## 3. DAOs and repositories

### 3.1 New DAOs (`data/local/dao/`)

| DAO | File | Main methods |
|-----|------|--------------|
| `GoalDao` | `GoalDao.kt` | insert, update, delete, getById, getAllActive, getAllFlow |
| `GoalItemDao` | `GoalItemDao.kt` | insert, update, delete, getByGoalId, getAllForGoalFlow |
| `QADao` | `QADao.kt` | insert, update, delete, getById, getBySubjectChapter, getByIds, countBySubjectChapter |
| `AssessmentDao` | `AssessmentDao.kt` | CRUD, getByGoalId, getAllFlow |
| `AssessmentQuestionDao` | `AssessmentQuestionDao.kt` | insertAll, getByAssessmentId, deleteByAssessmentId |
| `AttemptDao` | `AttemptDao.kt` | insert, update (end time), getByAssessmentId, getById |
| `AttemptAnswerDao` | `AttemptAnswerDao.kt` | insertAll, getByAttemptId |
| `ResultDao` | `ResultDao.kt` | insert, getByAttemptId |

### 3.2 New repositories (`data/repository/`)

- **GoalRepository**: orchestrate Goal + GoalItem; expose Flow for dashboard.
- **QABankRepository**: QA CRUD; “get N random by subject/chapter” for assessment creation; integrate with OcrHelper and parser.
- **AssessmentRepository**: create assessment (pick Qs, persist Assessment + AssessmentQuestions); list assessments; get assessment with questions (with QA loaded).
- **AttemptRepository**: start attempt, save answers, end attempt; load attempt + answers for grading and result screen.
- **ResultRepository**: save result (after grading); get result by attempt; aggregate by goal/subject/chapter for dashboard.

---

## 4. Sprint 1 — MVP (2–3 weeks)

### 4.1 Goals and goal items

- **Entities**: `Goal`, `GoalItem` + `GoalDao`, `GoalItemDao`.
- **Repository**: `GoalRepository`.
- **UI**:
  - **Goal list screen**: list active goals, “Add goal”, navigate to goal detail/dashboard.
  - **Goal create/edit screen**: name, description, exam date, add/remove goal items (subject + chapter list, optional target hours).
- **Navigation**: e.g. `NavRoutes.GOAL_LIST`, `GOAL_DETAIL`, `GOAL_EDIT`, `GOAL_ADD`; register in `AppNavGraph`.
- **Entry**: From HomeScreen (Study Tools tab) add a card/button “Exam goals” → `NavRoutes.GOAL_LIST`.

### 4.2 Q&A bank — basic scanner

- **Entity**: `QA` + `QuestionType` enum and converter; `QADao`.
- **Reuse**: `OcrHelper.extractTextFromImage(context, imageUri)` for OCR.
- **Heuristic parser** (new util or `data/qa/` package):
  - Input: raw OCR string. Output: list of `ParsedQA(question, answer, type, options?)`.
  - Implement: numbered pattern detection (`1.`, `Q1`, etc.), `?` and question-word detection, MCQ option patterns (`a)`, `A.`), fill-in (`____`), true/false keywords.
- **Flow**:
  1. **Scan screen**: camera/gallery → image URI → call OCR → call heuristic parser → show **editable QA list** (question, answer, type, options editable).
  2. **Save**: user assigns subject/chapter (and optional source id), save each item to `QADao` (Repository → insert).
- **UI**: Scan screen (capture/upload → progress → QA editor list → save); QA Bank screen (list by subject/chapter, search, delete). Navigation: `QA_SCAN`, `QA_EDIT`, `QA_BANK`.

### 4.3 Assessments — create and run (objective only)

- **Entities**: `Assessment`, `AssessmentQuestion`; **Attempt**, **AttemptAnswer**, **Result**.
- **DAOs**: as above.
- **Assessment creation UI**:
  - Choose source: by goal (then subject/chapter from goal items) or by subject/chapter directly; “manual” = pick from QA bank by selection.
  - Options: number of questions, time limit (total seconds), randomize (yes/no).
  - Preview: list of selected QAs; save → `AssessmentRepository.createAssessment(...)`.
- **Assessment run UI**:
  - Load assessment + questions (with QA); start attempt (create `Attempt`, start timer).
  - **Question-type-specific inputs (MVP)**: MCQ (radio), True/False (radio), numeric (number field), fill-in (single-line text). Short answer = single-line or multi-line text; no essay/voice/image in Sprint 1.
  - Timer at top; progress (answered/total); next/prev; submit → persist `AttemptAnswer` for each; set `Attempt.endedAt`; trigger grading.
- **Grading (MVP — objective only)**:
  - **Grading service** (e.g. `GradingService` or use case): for each `AttemptAnswer` load QA; normalize answer; MCQ/TF: exact option match; numeric: parse number + tolerance; fill-in: normalized string match or simple token overlap.
  - Compute `Result`: score, maxScore, percent, `detailsJson` (per-question correct/incorrect, feedback).
  - Persist `Result`; navigate to result screen.

### 4.4 Goal dashboard (basic)

- **Dashboard screen** (goal detail):
  - Show goal name, exam date, **days remaining**.
  - **Progress**: e.g. “X / Y questions practiced” (from QA bank counts or from attempts by subject/chapter — define metric: e.g. unique QAs attempted in this goal’s subjects/chapters).
  - Optional: `targetHours` from goal items vs “study hours logged” (from timetable or manual log): if you have study hours in the app, aggregate by subject; else show only “questions practiced” and “assessments taken”.
- **Metrics**: `completedQuestions` (or “QAs attempted”), `totalQuestions` (from QA bank for this goal’s subjects/chapters), `percentComplete`; list of recent assessments / attempts with scores.

### 4.5 Database and DI

- **AppDatabase**: add all 8 entities, version 3, Migration 2→3, TypeConverters for `QuestionType`.
- **AppModule**: provide new DAOs; no new backend deps in Sprint 1.
- **WorkManager**: optional in Sprint 1 (e.g. defer “heavy parse” if you add it later); not required for MVP flow.

### 4.6 Sprint 1 file checklist

| Layer | Action | Files |
|-------|--------|--------|
| Entity | Create | `Goal.kt`, `GoalItem.kt`, `QA.kt` (with QuestionType), `Assessment.kt`, `AssessmentQuestion.kt`, `Attempt.kt`, `AttemptAnswer.kt`, `Result.kt`, `QuestionTypeConverter.kt` |
| DAO | Create | `GoalDao.kt`, `GoalItemDao.kt`, `QADao.kt`, `AssessmentDao.kt`, `AssessmentQuestionDao.kt`, `AttemptDao.kt`, `AttemptAnswerDao.kt`, `ResultDao.kt` |
| DB | Modify | `AppDatabase.kt` (entities, version, migrations, DAOs, TypeConverters) |
| DI | Modify | `AppModule.kt` (provide new DAOs) |
| Repository | Create | `GoalRepository.kt`, `QABankRepository.kt`, `AssessmentRepository.kt`, `AttemptRepository.kt`, `ResultRepository.kt` |
| Parser | Create | `QaHeuristicParser.kt` (or `data/qa/HeuristicQaParser.kt`) |
| Grading | Create | `ObjectiveGradingService.kt` or grading in repository |
| UI — Goals | Create | `goallist/`, `goaldetail/`, `goaledit/` (Screen + ViewModel each) |
| UI — QA | Create | `qabank/`, `qascan/`, `qaeditor/` (or combined scan+editor) |
| UI — Assessment | Create | `assessmentcreate/`, `assessmentrun/`, `assessmentresult/` |
| Navigation | Modify | `NavRoutes.kt`, `AppNavGraph.kt` |
| Home | Modify | `HomeScreen.kt` (entry to Exam goals) |
| Strings | Modify | `strings.xml` (new labels) |

---

## 5. Sprint 2 — Voice, image, LLM parse, subjective grading (2–3 weeks)

### 5.1 Voice and image answer submission

- **Voice**: Use Android `SpeechRecognizer` (or existing STT if any); “press to record” → text → show transcribed text for user to confirm/edit → save as `answerText` (and optionally `answerVoiceUri` if you store the clip).
- **Image**: “Submit by photo” → capture/upload image → store URI in `AttemptAnswer.answerImageUri`; run OCR (OcrHelper) → prefill `answerText` for user to correct → then grade as text; or mark for “manual/LLM” grading.
- **UI**: In assessment run screen, add “Record answer” and “Upload handwritten answer” per question where type supports it (short/essay/numeric).

### 5.2 LLM-assisted QA parsing (opt-in)

- **Backend**: Small endpoint that accepts OCR text and returns JSON array of `{question, answer, type, options?, confidence}` (prompt as in design).
- **Client**: Optional “Improve with AI” on scan result: send OCR text to endpoint, replace heuristic result with LLM result; still show editable list before save.
- **Privacy**: Consent before upload; show “which data is uploaded” in settings/scan screen.

### 5.3 Subjective grading pipeline

- **Similarity**: Token overlap (normalize + token set ratio) for short answer; optional embedding similarity (backend) with thresholds (0.86 full, 0.68–0.86 partial).
- **LLM rubric**: Backend endpoint for short/essay: student + model answer → JSON `{score, feedback, confidence}`; use for partial credit and feedback.
- **Flow**: After attempt ends, for each subjective answer: run local token overlap first; if backend enabled, call LLM grading and merge; store grader type and confidence in `detailsJson`.

### 5.4 Store and show detailed results

- **Result screen**: Expand to show per-question: correct/partial/wrong, score, feedback, correct answer; link to “revise” (e.g. filter QA bank by subject/chapter) — remediation links in Sprint 3.

### 5.5 Sprint 2 file checklist

| Layer | Action | Files |
|-------|--------|--------|
| Network | Create | ExamGoalApi (Retrofit interface), LLM parse + grading endpoints |
| Repository | Modify/Create | QABankRepository (LLM parse call), GradingRepository or extend ResultRepository for LLM grading |
| Worker | Create | DeferredGradingWorker (queue subjective grading when online) |
| UI | Modify | Assessment run (voice + image inputs); Result screen (detailed breakdown) |
| Util | Create/Reuse | STT helper; reuse OcrHelper for image answer OCR |

---

## 6. Sprint 3 — CAS, remediation, prediction (2–3 weeks)

### 6.1 Math verification (CAS)

- **Backend**: SymPy (or similar) endpoint: compare key vs student answer (normalize → canonical form → equivalence).
- **Client**: For `QuestionType.NUMERIC` and math short-answer, send both to CAS; use result in grading (full/partial/wrong) and store in `detailsJson`.

### 6.2 Remediation engine

- **Data**: Use `Result.detailsJson` and QA metadata to identify weak subjects/chapters.
- **Logic**: Spaced repetition (SM-2 or simple “review in N days”) — schedule “practice set” from QA bank; optional integration with timetable (add “Revise X” block).
- **UI**: “Suggested practice” on dashboard or after result screen; “Add to timetable” for revision block.

### 6.3 Goal dashboard — prediction and advanced visuals

- **On-track / behind**: `requiredDailyHours = remainingTargetHours / daysUntilExam`; compare to actual average daily study hours (from timetable or manual log); show “On track” / “Behind” and deficit.
- **Visuals**: Progress bar per subject/chapter; trend sparkline of assessment scores; optional heatmap calendar (reuse or new component).

### 6.4 Alerts

- WorkManager or AlarmManager: if `daysRemaining < threshold` and `percentComplete < X`, schedule notification (“Urgent: exam in N days, coverage at Y%”).

---

## 7. Sprint 4 — Polish

- Gamification (badges, streaks), leaderboards (local or optional cloud).
- Teacher/manual grading mode: flag attempt for “manual review”, store override score and feedback.
- Export reports (PDF/Excel of attempts and scores).
- Cloud backup (opt-in) for goals, QA bank, attempts.

---

## 8. Testing strategy

- **Unit**: Parser (heuristic) — given OCR-like string, expect list of ParsedQA; normalization and scoring (exact match, token overlap, numeric tolerance).
- **Integration**: Scan → parse → edit → save → create assessment → run attempt → grade → result (one full flow with in-memory or test DB).
- **Dataset**: A few sample OCR snippets (mixed question types) in `androidTest` or `test` resources for regression.
- **Manual**: Teacher validation on sample subjective grading to tune LLM prompts and thresholds.

---

## 9. Privacy and storage

- **Default**: All data local (Room); no network for MVP.
- **Cloud opt-in**: Clear consent before uploading OCR/text for LLM parse or grading; settings: “Use cloud for parsing/grading”, “Delete cloud data”, “Auto-delete after X days”.
- **Sensitive fields**: Consider `EncryptedSharedPreferences` or Room encryption for tokens/keys; avoid logging full Q/A in production.

---

## 10. Dependencies to add (when needed)

| When | Dependency | Purpose |
|------|------------|--------|
| Sprint 1 | (none new) | Room, Compose, Hilt, ML Kit already present. |
| Sprint 2 | (optional) Gson/kotlinx.serialization | Already have Gson for Retrofit; use for optionsJson, detailsJson. |
| Sprint 2 | (backend) | LLM API, embedding API. |
| Sprint 3 | (backend) | SymPy/CAS. |
| Sprint 4 | Apache POI or similar | Export Excel reports. |

---

## 11. Summary

- **Sprint 1** delivers: Goals + Goal items (CRUD + dashboard), QA bank with OCR + heuristic parse + editable save, assessments (create from bank, run with timer and type-specific inputs), objective-only grading, basic goal dashboard with progress and time remaining.
- **Sprint 2** adds: Voice/image answers, LLM parse (opt-in), subjective grading (similarity + LLM), detailed result screen.
- **Sprint 3** adds: CAS for math, remediation/suggested practice, on-track/behind prediction, richer dashboard and alerts.
- **Sprint 4** adds: Polish, manual grading, export, cloud backup.

This plan keeps the existing app patterns (Room, Hilt, Compose, OcrHelper, navigation) and layers Exam Goal as a new feature set with clear entry from the Home screen and optional backend in later sprints.
