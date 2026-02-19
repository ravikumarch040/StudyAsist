# StudyAsist – Complete Test Cases Suite

Comprehensive test case list covering every feature and sub-feature. Use this for unit tests, integration tests, and UI/Espresso tests.

**Legend:**
- **P0** = Critical (blocking release)
- **P1** = High (core functionality)
- **P2** = Medium (nice to have)
- **Unit** = JUnit/Robolectric, no emulator
- **Integration** = Database/Repository, may need Hilt test
- **UI** = Espresso/Compose UI test, requires emulator

---

## 1. Utility Functions

| ID | Test Case | Preconditions | Steps | Expected Result | Prio | Type |
|----|-----------|---------------|-------|-----------------|------|------|
| TC-U01 | formatTimeMinutes - zero | - | Call formatTimeMinutes(0) | Returns "0:00" | P0 | Unit |
| TC-U02 | formatTimeMinutes - hour only | - | Call formatTimeMinutes(360) | Returns "6:00" | P0 | Unit |
| TC-U03 | formatTimeMinutes - with minutes | - | Call formatTimeMinutes(870) | Returns "14:30" | P0 | Unit |
| TC-U04 | formatTimeMinutes - pads minutes | - | Call formatTimeMinutes(545) | Returns "9:05" | P1 | Unit |
| TC-U05 | formatTimeMinutes - midnight | - | Call formatTimeMinutes(0) | Returns "0:00" | P0 | Unit |
| TC-U06 | timeToMinutes - zero | - | Call timeToMinutes(0, 0) | Returns 0 | P0 | Unit |
| TC-U07 | timeToMinutes - valid input | - | Call timeToMinutes(1, 30) | Returns 90 | P0 | Unit |
| TC-U08 | timeToMinutes - 23:59 | - | Call timeToMinutes(23, 59) | Returns 1439 | P1 | Unit |
| TC-U09 | daysUntil - future date | - | Call daysUntil(futureEpoch) | Returns positive days | P0 | Unit |
| TC-U10 | daysUntil - past date | - | Call daysUntil(pastEpoch) | Returns 0 | P0 | Unit |
| TC-U11 | formatRelativeTimeAgo - just now | - | Call with current time | Returns "Just now" | P1 | Unit |
| TC-U12 | formatRelativeTimeAgo - minutes ago | - | Call with 2 min ago | Returns "2 min ago" | P1 | Unit |
| TC-U13 | formatRelativeTimeAgo - hours ago | - | Call with 3 hr ago | Returns "3 hr ago" | P1 | Unit |
| TC-U14 | formatRelativeTimeAgo - days ago | - | Call with 2 days ago | Returns "2 days ago" | P1 | Unit |
| TC-U15 | formatExamDate - valid epoch | - | Call formatExamDate(validMillis) | Returns formatted date string | P1 | Unit |
| TC-U16 | formatExamDate - edge dates | - | Test Dec 31, Jan 1 | Correct year boundary | P2 | Unit |

---

## 2. SM-2 Spaced Repetition Algorithm

| ID | Test Case | Preconditions | Steps | Expected Result | Prio | Type |
|----|-----------|---------------|-------|-----------------|------|------|
| TC-SM01 | sm2 - quality 0 (Again) | EF=2.5, interval=6, reps=3 | Call sm2(0, 2.5, 6, 3) | interval=1, reps=0, EF decreased | P0 | Unit |
| TC-SM02 | sm2 - quality 1 (Hard) | EF=2.5, interval=6, reps=2 | Call sm2(1, 2.5, 6, 2) | interval=1, reps=0 | P0 | Unit |
| TC-SM03 | sm2 - quality 2 (Good) first review | EF=2.5, interval=0, reps=0 | Call sm2(2, 2.5, 0, 0) | interval=1, reps=1 | P0 | Unit |
| TC-SM04 | sm2 - quality 2 (Good) second review | EF=2.6, interval=1, reps=1 | Call sm2(2, 2.6, 1, 1) | interval=6, reps=2 | P0 | Unit |
| TC-SM05 | sm2 - quality 3 (Easy) first review | EF=2.5, interval=0, reps=0 | Call sm2(3, 2.5, 0, 0) | interval=1, reps=1 | P0 | Unit |
| TC-SM06 | sm2 - quality out of range coerced | quality=5 | Call sm2(5, 2.5, 0, 0) | Treated as valid (3) | P1 | Unit |
| TC-SM07 | sm2 - EF never below 1.3 | Many Again ratings | Call sm2(0, ...) repeatedly | EF >= 1.3 | P0 | Unit |
| TC-SM08 | sm2 - interval never below 1 | - | Call sm2 with various inputs | interval >= 1 | P0 | Unit |

---

## 3. Objective Grading Service

| ID | Test Case | Preconditions | Steps | Expected Result | Prio | Type |
|----|-----------|---------------|-------|-----------------|------|------|
| TC-G01 | MCQ - exact match | QA with answer "Paris" | Grade user answer "Paris" | Full credit (1.0) | P0 | Unit |
| TC-G02 | MCQ - option letter match | QA with options A,B,C,D, answer C | Grade "c" | Full credit | P0 | Unit |
| TC-G03 | MCQ - wrong answer | QA answer "A" | Grade "B" | Zero credit | P0 | Unit |
| TC-G04 | TrueFalse - true match | QA answer "True" | Grade "true" | Full credit | P0 | Unit |
| TC-G05 | TrueFalse - yes matches true | QA answer "True" | Grade "yes" | Full credit | P1 | Unit |
| TC-G06 | Short - token overlap | QA answer "gravity" | Grade "force of gravity" | Partial/full per overlap | P0 | Unit |
| TC-G07 | Numeric - exact match | QA answer "42" | Grade "42" | Full credit | P0 | Unit |
| TC-G08 | Numeric - decimal equivalence | QA answer "0.5" | Grade "1/2" | Full credit (math equivalence) | P0 | Unit |
| TC-G09 | Numeric - tolerance | QA answer "10", tolerance 0.1 | Grade "10.05" | Full credit | P1 | Unit |
| TC-G10 | Essay - requires AI/cloud | useCloudForGrading=true | Grade essay answer | Partial credit from Gemini or fallback | P1 | Integration |
| TC-G11 | Empty answer | Any QA | Grade "" | Zero credit | P0 | Unit |
| TC-G12 | Multiple questions | 3 QAs | Grade 3 answers | Aggregate score correct | P0 | Unit |

---

## 4. Heuristic Q&A Parser

| ID | Test Case | Preconditions | Steps | Expected Result | Prio | Type |
|----|-----------|---------------|-------|-----------------|------|------|
| TC-QP01 | Parse MCQ format | Text with A) B) C) options | Call parser | Extracts question and options | P0 | Unit |
| TC-QP02 | Parse True/False | Text "True or False: ..." | Call parser | Extracts as TRUE_FALSE | P1 | Unit |
| TC-QP03 | Parse numeric | Text "Answer: 42" | Call parser | Extracts as NUMERIC | P1 | Unit |
| TC-QP04 | Parse short answer | Plain Q&A text | Call parser | Extracts as SHORT | P0 | Unit |
| TC-QP05 | No questions in text | Random text | Call parser | Returns empty list | P0 | Unit |
| TC-QP06 | Multiple questions | Text with 5 Q&As | Call parser | Returns 5 QA objects | P0 | Unit |

---

## 5. Focus Guard Helper

| ID | Test Case | Preconditions | Steps | Expected Result | Prio | Type |
|----|-----------|---------------|-------|-----------------|------|------|
| TC-FG01 | Is restricted - built-in package | Package in FOCUS_GUARD_RESTRICTED_PACKAGES | Call isRestricted | true | P0 | Unit |
| TC-FG02 | Is restricted - custom package | Package in extra list | Call isRestricted | true | P0 | Unit |
| TC-FG03 | Not restricted - launcher | System launcher package | Call isRestricted | false | P0 | Unit |
| TC-FG04 | Not restricted - study app | StudyAsist package | Call isRestricted | false | P0 | Unit |
| TC-FG05 | Add custom package | Empty extra list | addPackage("com.game") | Package in list | P1 | Unit |
| TC-FG06 | Remove custom package | Package in list | removePackage | Package removed | P1 | Unit |

---

## 6. Backup & Restore

| ID | Test Case | Preconditions | Steps | Expected Result | Prio | Type |
|----|-----------|---------------|-------|-----------------|------|------|
| TC-BK01 | Export to JSON | DB with timetables, goals, QA | exportToJson() | Valid JSON string | P0 | Integration |
| TC-BK02 | Import from JSON | Valid backup JSON | importFromJson(json) | All data restored | P0 | Integration |
| TC-BK03 | Export exam data only | DB with goals, QA, assessments | exportExamDataToJson() | JSON with exam data only | P1 | Integration |
| TC-BK04 | Import exam data | Valid exam JSON | importExamDataFromJson(json) | Exam data restored, replaces existing | P1 | Integration |
| TC-BK05 | Import invalid JSON | Malformed JSON | importFromJson(bad) | Result failure, no crash | P0 | Integration |
| TC-BK06 | Import empty JSON | "{}" | importFromJson | Handled gracefully | P1 | Integration |
| TC-BK07 | Export empty DB | Fresh DB | exportToJson() | Valid minimal JSON | P0 | Integration |
| TC-BK08 | Round-trip | DB with mixed data | Export then import | Data matches | P0 | Integration |

---

## 7. Share Repository (Assessment Share Codes)

| ID | Test Case | Preconditions | Steps | Expected Result | Prio | Type |
|----|-----------|---------------|-------|-----------------|------|------|
| TC-SH01 | Generate share code | Assessment with questions | generateShareCode(assessmentId) | Base64 string | P0 | Integration |
| TC-SH02 | Generate for non-existent | assessmentId=-1 | generateShareCode | Returns null | P0 | Integration |
| TC-SH03 | Decode share code | Valid code from TC-SH01 | decodeShareCode(code) | ShareableAssessment with questions | P0 | Unit |
| TC-SH04 | Decode invalid code | Garbage string | decodeShareCode | Returns null | P0 | Unit |
| TC-SH05 | Decode malformed Base64 | Invalid Base64 | decodeShareCode | Returns null, no crash | P1 | Unit |

---

## 8. Timetable Repository

| ID | Test Case | Preconditions | Steps | Expected Result | Prio | Type |
|----|-----------|---------------|-------|-----------------|------|------|
| TC-TT01 | Create timetable | - | createTimetable("Test", MON_SUN) | Returns id, exists in DB | P0 | Integration |
| TC-TT02 | Get all timetables | 2 timetables in DB | getAllTimetables() | Flow emits 2 | P0 | Integration |
| TC-TT03 | Get timetable by id | Timetable exists | getTimetable(id) | Returns entity | P0 | Integration |
| TC-TT04 | Get non-existent | id=999 | getTimetable(999) | Returns null | P0 | Integration |
| TC-TT05 | Update timetable | Timetable exists | updateTimetable(modified) | Persisted | P0 | Integration |
| TC-TT06 | Delete timetable | Timetable with activities | deleteTimetable(id) | Deleted, activities cascade | P0 | Integration |
| TC-TT07 | Duplicate timetable | Timetable with activities | duplicateTimetable(srcId, "Copy") | New timetable with copied activities | P0 | Integration |
| TC-TT08 | Export CSV | Timetable with activities | getExportCsv(id) | Valid CSV string | P1 | Integration |
| TC-TT09 | Export PDF | Timetable with activities | getExportPdf(id) | Non-empty ByteArray | P1 | Integration |
| TC-TT10 | Export Excel | Timetable with activities | getExportExcel(id) | Valid Excel bytes | P1 | Integration |

---

## 9. Activity Repository

| ID | Test Case | Preconditions | Steps | Expected Result | Prio | Type |
|----|-----------|---------------|-------|-----------------|------|------|
| TC-AC01 | Insert activity | Timetable exists | insertActivity(entity) | Returns id | P0 | Integration |
| TC-AC02 | Get activities for day | Activities on Mon | getActivitiesForDay(tid, 1) | Returns list | P0 | Integration |
| TC-AC03 | Has overlap - no overlap | Activity 9-10 | hasOverlap(tid, 1, 600, 660) | false | P0 | Integration |
| TC-AC04 | Has overlap - overlaps | Activity 9-10, check 9:30-10:30 | hasOverlap | true | P0 | Integration |
| TC-AC05 | Copy day to day | Mon has 3 activities | copyDayToDay(tid, 1, 2) | Tue has same 3 | P0 | Integration |
| TC-AC06 | Update activity | Activity exists | updateActivity(modified) | Persisted | P0 | Integration |
| TC-AC07 | Delete activity | Activity exists | deleteActivity(id) | Removed | P0 | Integration |

---

## 10. Goal Repository

| ID | Test Case | Preconditions | Steps | Expected Result | Prio | Type |
|----|-----------|---------------|-------|-----------------|------|------|
| TC-GR01 | Create goal | - | createGoal(name, date, items) | Returns id | P0 | Integration |
| TC-GR02 | Get all goals | 2 goals | getAllGoals() | Flow emits 2 | P0 | Integration |
| TC-GR03 | Get active goals | 1 active, 1 past | getAllActiveGoals() | Flow emits 1 | P0 | Integration |
| TC-GR04 | Update goal | Goal exists | updateGoal(modified) | Persisted | P0 | Integration |
| TC-GR05 | Add goal item | Goal exists | addGoalItem(goalId, subj, ch) | Item added | P0 | Integration |
| TC-GR06 | Update goal items | Goal has items | updateGoalItems(goalId, new) | Replaced | P0 | Integration |
| TC-GR07 | Delete goal | Goal exists | deleteGoal(id) | Removed | P0 | Integration |

---

## 11. Q&A Bank Repository

| ID | Test Case | Preconditions | Steps | Expected Result | Prio | Type |
|----|-----------|---------------|-------|-----------------|------|------|
| TC-QA01 | Insert QA | - | insertQA(qa) | Returns id | P0 | Integration |
| TC-QA02 | Get by subject/chapter | QAs with subject "Math" | getQABySubjectChapter("Math", null) | Filtered list | P0 | Integration |
| TC-QA03 | Get distinct subjects | 3 subjects in DB | getDistinctSubjects() | List of 3 | P0 | Integration |
| TC-QA04 | Get distinct chapters | For subject "Math" | getDistinctChaptersForSubject("Math") | Chapter list | P0 | Integration |
| TC-QA05 | Get random QA | 10 QAs in DB | getRandomQA(null, null, 5) | 5 random QAs | P0 | Integration |
| TC-QA06 | Count QA | 7 QAs for subject | countQA("Math", null) | 7 | P0 | Integration |
| TC-QA07 | Insert batch | List of 5 QAs | insertQABatch(list) | All inserted | P1 | Integration |
| TC-QA08 | Delete QA | QA exists | deleteQA(id) | Removed | P0 | Integration |
| TC-QA09 | Get due cards (SRS) | QAs with nextReviewDate <= today | getDueCards(now, 20) | Due cards | P0 | Integration |
| TC-QA10 | Update SRS fields | QA with SRS data | updateSRS(...) | Updated | P0 | Integration |

---

## 12. Assessment Repository

| ID | Test Case | Preconditions | Steps | Expected Result | Prio | Type |
|----|-----------|---------------|-------|-----------------|------|------|
| TC-AS01 | Create assessment | Goal, QAs exist | createAssessment(...) | Returns id | P0 | Integration |
| TC-AS02 | Create from random | 20 QAs | createAssessmentFromRandom(goalId, 10) | Assessment with 10 random | P0 | Integration |
| TC-AS03 | Create from goal | Goal with subjects | createAssessmentFromGoal(goalId) | Assessment from goal QAs | P0 | Integration |
| TC-AS04 | Get with questions | Assessment exists | getAssessmentWithQuestions(id) | AssessmentWithQuestions | P0 | Integration |
| TC-AS05 | Update assessment | Assessment exists | updateAssessment(modified) | Persisted | P0 | Integration |
| TC-AS06 | Delete assessment | Assessment exists | deleteAssessment(id) | Removed, attempts cascade | P0 | Integration |
| TC-AS07 | Create retry | Attempt with wrongs | createRetryAssessment(attemptId, "weak") | New assessment, weak only | P1 | Integration |

---

## 13. Attempt Repository

| ID | Test Case | Preconditions | Steps | Expected Result | Prio | Type |
|----|-----------|---------------|-------|-----------------|------|------|
| TC-AT01 | Start attempt | Assessment exists | startAttempt(assessmentId) | Returns attemptId | P0 | Integration |
| TC-AT02 | End attempt | Attempt in progress | endAttempt(attemptId) | endedAt set | P0 | Integration |
| TC-AT03 | Save answers | Attempt started | saveAnswers(attemptId, [...]) | Answers stored | P0 | Integration |
| TC-AT04 | Get answers | Attempt with answers | getAnswers(attemptId) | Returns list | P0 | Integration |
| TC-AT05 | Set needs manual review | Attempt exists | setNeedsManualReview(id, true) | Flag set | P0 | Integration |
| TC-AT06 | Get needing manual review | 2 attempts flagged | getNeedingManualReview() | Returns 2 | P0 | Integration |

---

## 14. Result Repository

| ID | Test Case | Preconditions | Steps | Expected Result | Prio | Type |
|----|-----------|---------------|-------|-----------------|------|------|
| TC-RS01 | Save result | Attempt completed, answers | saveResult(...) | Result stored | P0 | Integration |
| TC-RS02 | Get result | Result exists | getResult(attemptId) | Returns result | P0 | Integration |
| TC-RS03 | Get top results | 5 results | getTopResultListItems(5) | 5 items | P0 | Integration |
| TC-RS04 | Export CSV | Results exist | getExportCsv() | Valid CSV | P1 | Integration |
| TC-RS05 | Export PDF | Results exist | getExportPdf() | Valid PDF bytes | P1 | Integration |
| TC-RS06 | Export Excel | Results exist | getExportExcel() | Valid Excel | P1 | Integration |
| TC-RS07 | Export PDF for attempt | Attempt has result | getExportPdfForAttempt(id) | PDF bytes | P1 | Integration |

---

## 15. Settings Repository

| ID | Test Case | Preconditions | Steps | Expected Result | Prio | Type |
|----|-----------|---------------|-------|-----------------|------|------|
| TC-ST01 | Set default lead minutes | - | setDefaultLeadMinutes(15) | Persisted | P0 | Integration |
| TC-ST02 | Set vibration | - | setVibrationEnabled(true) | Persisted | P1 | Integration |
| TC-ST03 | Set user name | - | setUserName("Test") | Persisted | P0 | Integration |
| TC-ST04 | Set Gemini API key | - | setGeminiApiKey("key") | Persisted | P0 | Integration |
| TC-ST05 | Set dark mode | - | setDarkMode("dark") | Persisted | P0 | Integration |
| TC-ST06 | Set app locale | - | setAppLocale("hi") | Persisted | P0 | Integration |
| TC-ST07 | Set theme id | - | setThemeId("MINIMAL_LIGHT") | Persisted | P0 | Integration |
| TC-ST08 | Set onboarding completed | - | setOnboardingCompleted(true) | Persisted | P0 | Integration |
| TC-ST09 | Add focus guard package | - | addFocusGuardPackage("pkg") | In extra list | P1 | Integration |
| TC-ST10 | Remove focus guard package | Package in list | removeFocusGuardPackage("pkg") | Removed | P1 | Integration |
| TC-ST11 | Set Pomodoro durations | - | setPomodoroFocusMinutes(45) | Persisted | P1 | Integration |
| TC-ST12 | Set exam alert thresholds | - | setExamGoalAlertDaysThreshold(7) | Persisted | P1 | Integration |

---

## 16. SRS Repository

| ID | Test Case | Preconditions | Steps | Expected Result | Prio | Type |
|----|-----------|---------------|-------|-----------------|------|------|
| TC-SR01 | Get due cards | QAs with nextReviewDate <= now | getDueCards(10) | List of due cards | P0 | Integration |
| TC-SR02 | Get due count | 5 due cards | getDueCount() | 5 | P0 | Integration |
| TC-SR03 | Process review - Again | QA with SRS data | processReview(qaId, 0) | nextReviewDate tomorrow, reps=0 | P0 | Integration |
| TC-SR04 | Process review - Good | QA first time | processReview(qaId, 2) | interval=1, reps=1 | P0 | Integration |
| TC-SR05 | Process review - Easy | QA reps=1 | processReview(qaId, 3) | interval=6, reps=2 | P0 | Integration |
| TC-SR06 | Get review forecast | QAs with future reviews | getReviewForecast(7) | Map of date to count | P1 | Integration |

---

## 17. Streak & Badge Repository

| ID | Test Case | Preconditions | Steps | Expected Result | Prio | Type |
|----|-----------|---------------|-------|-----------------|------|------|
| TC-SB01 | Get current streak | 3 days consecutive | getCurrentStreak() | 3 | P0 | Integration |
| TC-SB02 | Get current streak - none | No recent attempts | getCurrentStreak() | 0 | P0 | Integration |
| TC-SB03 | Check and award - first assessment | No badges | checkAndAwardAfterAttempt(attemptId) | First Step awarded | P0 | Integration |
| TC-SB04 | Check and award - perfect | 100% attempt | checkAndAwardAfterAttempt | Perfect badge | P1 | Integration |
| TC-SB05 | Check streak badges | 7 day streak | checkAndAwardStreakBadges(7) | Week Warrior if not earned | P1 | Integration |
| TC-SB06 | Get earned badges | 2 badges earned | getEarnedBadgesOnce() | 2 items | P0 | Integration |

---

## 18. Timetable Templates

| ID | Test Case | Preconditions | Steps | Expected Result | Prio | Type |
|----|-----------|---------------|-------|-----------------|------|------|
| TC-TM01 | Generic school day | - | TimetableTemplates.genericSchoolDay | 5 days × 8 activities | P1 | Unit |
| TC-TM02 | Weekend study | - | TimetableTemplates.weekendStudy | 2 days activities | P1 | Unit |
| TC-TM03 | Exam prep | - | TimetableTemplates.examPrep | 7 days activities | P1 | Unit |
| TC-TM04 | Template activity structure | - | Access TemplateActivity fields | dayOfWeek, startHour, title, type valid | P1 | Unit |

---

## 19. Navigation & UI Flows

| ID | Test Case | Preconditions | Steps | Expected Result | Prio | Type |
|----|-----------|---------------|-------|-----------------|------|------|
| TC-UI01 | Bottom nav - all 5 tabs | App launched | Tap Home, Timetable, Study, Goals, More | Each screen shown | P0 | UI |
| TC-UI02 | Home → Timetable detail | Timetable exists | Tap timetable card | Timetable detail opens | P0 | UI |
| TC-UI03 | Home → Goal detail | Goal exists | Tap goal card | Goal detail opens | P0 | UI |
| TC-UI04 | Home → Result detail | Result exists | Tap last result | Result screen opens | P0 | UI |
| TC-UI05 | Quick actions - Dictate | - | Tap Dictate chip | Dictate screen opens | P0 | UI |
| TC-UI06 | Quick actions - Explain | - | Tap Explain | Explain screen opens | P0 | UI |
| TC-UI07 | Quick actions - Solve | - | Tap Solve | Solve screen opens | P0 | UI |
| TC-UI08 | Quick actions - Assessments | - | Tap Assessments | Assessment list opens | P0 | UI |
| TC-UI09 | Study Hub - all menu items | - | Tap each hub item | Correct screen opens | P0 | UI |
| TC-UI10 | More Hub - Settings | - | Tap Settings | Settings opens | P0 | UI |
| TC-UI11 | Settings - User Guide | - | Tap User Guide card | User Guide screen opens | P1 | UI |
| TC-UI12 | Back navigation | On detail screen | Press back | Returns to previous | P0 | UI |
| TC-UI13 | Bottom bar hidden on run | - | Navigate to Assessment Run | Bottom bar hidden | P0 | UI |
| TC-UI14 | Bottom bar hidden on QA Scan | - | Navigate to QA Scan | Bottom bar hidden | P0 | UI |

---

## 20. Onboarding

| ID | Test Case | Preconditions | First launch | Steps | Expected Result | Prio | Type |
|----|-----------|---------------|--------------|------|-----------------|------|------|
| TC-OB01 | Onboarding shown first launch | onboardingCompleted=false | - | Launch app | Onboarding pager shown | P0 | UI |
| TC-OB02 | Onboarding skip | On onboarding | Tap Skip | Skip to name page | P1 | UI |
| TC-OB03 | Onboarding next | On page 1 | Tap Next 4 times | Reaches name page | P0 | UI |
| TC-OB04 | Onboarding complete | On name page | Enter name, Get Started | onboardingCompleted=true, Home shown | P0 | UI |
| TC-OB05 | Onboarding not shown | onboardingCompleted=true | - | Launch app | Home shown directly | P0 | UI |

---

## 21. Timetable Screens

| ID | Test Case | Preconditions | Steps | Expected Result | Prio | Type |
|----|-----------|---------------|-------|-----------------|------|------|
| TC-TS01 | Timetable list - create | - | Tap +, enter name, Create | New timetable in list | P0 | UI |
| TC-TS02 | Timetable list - set active | 2 timetables | Tap Set active | Active indicator shown | P0 | UI |
| TC-TS03 | Timetable list - duplicate | Timetable exists | Tap options, Duplicate | Copy created | P0 | UI |
| TC-TS04 | Timetable list - delete | Timetable exists | Tap options, Delete | Removed | P0 | UI |
| TC-TS05 | Timetable detail - Day view | Timetable with activities | Open, Day tab | Activities listed | P0 | UI |
| TC-TS06 | Timetable detail - Week view | Timetable with activities | Switch to Week | Grid shown | P0 | UI |
| TC-TS07 | Timetable detail - Timeline view | Timetable with activities | Switch to Timeline | Timeline with dots | P1 | UI |
| TC-TS08 | Timetable detail - filter by type | Mixed activities | Select Study filter | Only Study shown | P1 | UI |
| TC-TS09 | Add activity | On timetable | Tap +, fill form, Save | Activity added | P0 | UI |
| TC-TS10 | Edit activity | Activity exists | Tap activity, edit, Save | Updated | P0 | UI |
| TC-TS11 | Copy from day | Mon has activities | Copy from Mon to Tue | Tue has copies | P1 | UI |
| TC-TS12 | Overlap warning | Block overlap on | Add overlapping activity | Warning shown | P1 | UI |

---

## 22. Goal Screens

| ID | Test Case | Preconditions | Steps | Expected Result | Prio | Type |
|----|-----------|---------------|-------|-----------------|------|------|
| TC-GS01 | Goal list - add goal | - | Tap FAB, fill form, Save | Goal in list | P0 | UI |
| TC-GS02 | Goal list - tap goal | Goal exists | Tap goal card | Goal detail opens | P0 | UI |
| TC-GS03 | Goal detail - edit | On goal detail | Tap edit | Goal edit opens | P0 | UI |
| TC-GS04 | Goal detail - create assessment | Goal with QAs | Tap Create Assessment | Assessment create for goal | P0 | UI |
| TC-GS05 | Goal detail - add to timetable | - | Tap Add to Timetable on topic | Add revision screen | P1 | UI |
| TC-GS06 | Goal detail - quick practice | Assessment for goal | Tap Quick Practice | Assessment run starts | P1 | UI |
| TC-GS07 | Goal detail - score trend | Attempts exist | View trend | Sparkline/line shown | P1 | UI |
| TC-GS08 | Goal detail - suggested practice | Wrong answers exist | View suggested | Topics listed | P1 | UI |

---

## 23. Q&A Bank & Scan

| ID | Test Case | Preconditions | Steps | Expected Result | Prio | Type |
|----|-----------|---------------|-------|-----------------|------|------|
| TC-QS01 | Q&A list - empty state | No QAs | Open Q&A Bank | Empty message shown | P0 | UI |
| TC-QS02 | Q&A list - filter subject | QAs with subjects | Select subject | Filtered list | P0 | UI |
| TC-QS03 | Q&A list - filter chapter | - | Select chapter | Filtered list | P0 | UI |
| TC-QS04 | Q&A list - scan FAB | - | Tap Scan FAB | QA Scan screen | P0 | UI |
| TC-QS05 | Q&A list - create assessment | QAs exist | Tap Create Assessment (top) | Assessment create | P0 | UI |
| TC-QS06 | QA Scan - take photo | Camera permission | Tap Take photo | Camera opens | P0 | UI |
| TC-QS07 | QA Scan - upload image | - | Tap Upload | Picker opens | P0 | UI |
| TC-QS08 | QA Scan - extract | Image with text | Extract | Text extracted | P0 | UI |
| TC-QS09 | QA Scan - improve with AI | useCloudForParsing on | Tap Improve with AI | AI improves, QAs extracted | P1 | UI |
| TC-QS10 | QA Scan - manual add row | - | Tap Add row, fill Q&A | Row added | P0 | UI |
| TC-QS11 | QA Scan - save to bank | Rows filled | Tap Save to bank | QAs in DB | P0 | UI |

---

## 24. Assessment Flow

| ID | Test Case | Preconditions | Steps | Expected Result | Prio | Type |
|----|-----------|---------------|-------|-----------------|------|------|
| TC-AF01 | Assessment list - empty | No assessments | Open list | Empty message | P0 | UI |
| TC-AF02 | Assessment list - create FAB | - | Tap FAB | Create screen | P0 | UI |
| TC-AF03 | Assessment create - by goal | Goal with QAs | Select goal, add questions | Assessment created | P0 | UI |
| TC-AF04 | Assessment create - by subject | QAs exist | Select subject/chapter, add | Assessment created | P0 | UI |
| TC-AF05 | Assessment create - manual | QAs exist | Select questions manually | Assessment created | P0 | UI |
| TC-AF06 | Assessment run - start | Assessment exists | Tap Play | Run screen, timer | P0 | UI |
| TC-AF07 | Assessment run - next/prev | On run | Tap Next, Previous | Navigate questions | P0 | UI |
| TC-AF08 | Assessment run - submit | All answered | Tap Submit | Result screen | P0 | UI |
| TC-AF09 | Assessment run - voice answer | Mic permission | Tap Record, speak | Answer captured | P1 | UI |
| TC-AF10 | Assessment run - image answer | - | Tap Photo, capture | Answer from image | P1 | UI |
| TC-AF11 | Assessment edit | Assessment exists | Tap Edit | Edit screen | P0 | UI |
| TC-AF12 | Assessment delete | Assessment exists | Tap Delete, confirm | Removed | P0 | UI |

---

## 25. Result & Manual Review

| ID | Test Case | Preconditions | Steps | Expected Result | Prio | Type |
|----|-----------|---------------|-------|-----------------|------|------|
| TC-RF01 | Result list - sort | Results exist | Change sort | List reordered | P0 | UI |
| TC-RF02 | Result list - filter | Results exist | Filter by assessment | Filtered | P0 | UI |
| TC-RF03 | Result detail - revise | On result | Tap Revise | Q&A bank filtered | P1 | UI |
| TC-RF04 | Result detail - add to timetable | On result | Tap Add to Timetable | Add revision | P1 | UI |
| TC-RF05 | Result detail - manual review | On result | Tap Manual Review | Manual override screen | P0 | UI |
| TC-RF06 | Result detail - retry | On result | Tap Retry | New attempt starts | P1 | UI |
| TC-RF07 | Result detail - export | On result | Tap Export, PDF | PDF generated | P1 | UI |
| TC-RF08 | Manual override - change score | On override screen | Change score, Save | Updated | P0 | UI |
| TC-RF09 | Manual override - feedback | On override screen | Add feedback, Save | Saved | P1 | UI |

---

## 26. Daily Review & Flashcards

| ID | Test Case | Preconditions | Steps | Expected Result | Prio | Type |
|----|-----------|---------------|-------|-----------------|------|------|
| TC-DR01 | Daily review - no due cards | No due cards | Open Daily Review | Empty/complete message | P0 | UI |
| TC-DR02 | Daily review - due cards | Due cards exist | Open | Cards shown | P0 | UI |
| TC-DR03 | Daily review - tap flip | Card shown | Tap card | Answer revealed | P0 | UI |
| TC-DR04 | Daily review - rate Again | Card shown | Tap Again | Next card, SRS updated | P0 | UI |
| TC-DR05 | Daily review - rate Good | Card shown | Tap Good | Next card | P0 | UI |
| TC-DR06 | Daily review - session complete | Last card | Rate last | Summary shown | P0 | UI |
| TC-DR07 | Flashcards - swipe right | Card shown | Swipe right | Correct, next | P0 | UI |
| TC-DR08 | Flashcards - swipe left | Card shown | Swipe left | Wrong, next | P0 | UI |
| TC-DR09 | Flashcards - tap flip | Card shown | Tap | Answer shown | P0 | UI |
| TC-DR10 | Flashcards - filter | Cards for subjects | Select filter | Filtered cards | P1 | UI |

---

## 27. Pomodoro

| ID | Test Case | Preconditions | Steps | Expected Result | Prio | Type |
|----|-----------|---------------|-------|-----------------|------|------|
| TC-PM01 | Pomodoro - start | - | Tap Start | Timer runs | P0 | UI |
| TC-PM02 | Pomodoro - pause | Timer running | Tap Pause | Timer paused | P0 | UI |
| TC-PM03 | Pomodoro - resume | Timer paused | Tap Resume | Timer continues | P0 | UI |
| TC-PM04 | Pomodoro - skip | In focus | Tap Skip | Short break starts | P0 | UI |
| TC-PM05 | Pomodoro - reset | Timer running | Tap Reset | Timer reset | P0 | UI |
| TC-PM06 | Pomodoro - complete focus | Focus duration elapsed | Wait | Break phase | P0 | UI |
| TC-PM07 | Pomodoro - total today | 2 sessions completed | View total | Shows sum | P1 | UI |

---

## 28. AI Study Plan & Tutor

| ID | Test Case | Preconditions | Steps | Expected Result | Prio | Type |
|----|-----------|---------------|-------|-----------------|------|------|
| TC-AI01 | Study plan - generate | API key set | Enter goal, subjects, date; Generate | Plan shown | P1 | UI |
| TC-AI02 | Study plan - no API key | No key | Tap Generate | Error or empty | P1 | UI |
| TC-AI03 | AI Tutor - send message | API key set | Type, Send | AI response shown | P1 | UI |
| TC-AI04 | AI Tutor - loading | - | Send message | Loading indicator | P1 | UI |
| TC-AI05 | AI Tutor - history | Messages sent | Scroll | History visible | P1 | UI |

---

## 29. Settings Screens

| ID | Test Case | Preconditions | Steps | Expected Result | Prio | Type |
|----|-----------|---------------|-------|-----------------|------|------|
| TC-SE01 | Settings - user name | - | Change name | Persisted | P0 | UI |
| TC-SE02 | Settings - profile photo | - | Tap avatar, pick photo | Photo set | P1 | UI |
| TC-SE03 | Settings - language | - | Select Hindi | App in Hindi | P0 | UI |
| TC-SE04 | Settings - dark mode | - | Select Dark | Dark theme | P0 | UI |
| TC-SE05 | Settings - theme | - | Select theme | Theme applied | P0 | UI |
| TC-SE06 | Settings - default reminder | - | Select 15 min | Persisted | P0 | UI |
| TC-SE07 | Settings - vibration | - | Toggle | Persisted | P1 | UI |
| TC-SE08 | Settings - block overlap | - | Toggle | Persisted | P1 | UI |
| TC-SE09 | Settings - exam alert days | - | Select 7 days | Persisted | P1 | UI |
| TC-SE10 | Settings - exam alert percent | - | Select 50% | Persisted | P1 | UI |
| TC-SE11 | Settings - focus guard | - | Toggle on | Usage access prompt | P1 | UI |
| TC-SE12 | Settings - focus guard custom | Focus guard on | Add package | Package in list | P1 | UI |
| TC-SE13 | Settings - TTS voice | - | Select voice | Persisted | P1 | UI |
| TC-SE14 | Settings - API key | - | Paste key | Persisted | P0 | UI |
| TC-SE15 | Settings - test API key | Key entered | Tap Test | Success or error | P0 | UI |
| TC-SE16 | Settings - cloud backup target | - | Select Folder/Drive | Persisted | P1 | UI |
| TC-SE17 | Settings - backup | Folder set | Tap Backup | Export triggered | P0 | UI |
| TC-SE18 | Settings - restore | Backup file | Tap Restore, pick file | Import, success message | P0 | UI |
| TC-SE19 | Settings - export exam data | - | Tap Export exam data | File picker | P1 | UI |
| TC-SE20 | Settings - restore exam data | Exam JSON | Tap Restore exam data | Import | P1 | UI |

---

## 30. Add Revision

| ID | Test Case | Preconditions | Steps | Expected Result | Prio | Type |
|----|-----------|---------------|-------|-----------------|------|------|
| TC-AR01 | Add revision - select timetable | Timetables exist | Open, select timetable | Timetable chosen | P0 | UI |
| TC-AR02 | Add revision - day and time | - | Pick day, start, end | Values set | P0 | UI |
| TC-AR03 | Add revision - save | Form filled | Tap Save | Activity in timetable | P0 | UI |

---

## 31. Export & Share

| ID | Test Case | Preconditions | Steps | Expected Result | Prio | Type |
|----|-----------|---------------|-------|-----------------|------|------|
| TC-EX01 | Export timetable CSV | Timetable with activities | Export CSV | File saved/shared | P1 | UI |
| TC-EX02 | Export timetable PDF | Timetable with activities | Export PDF | PDF generated | P1 | UI |
| TC-EX03 | Share timetable image | Timetable | Share as image | Image shared | P1 | UI |
| TC-EX04 | Print timetable | Timetable | Print | Print dialog | P1 | UI |
| TC-EX05 | Export results CSV | Results exist | Export CSV | File | P1 | UI |
| TC-EX06 | Share assessment code | Assessment exists | Generate share | Code generated | P1 | UI |

---

## 32. Widgets

| ID | Test Case | Preconditions | Steps | Expected Result | Prio | Type |
|----|-----------|---------------|-------|-----------------|------|------|
| TC-WD01 | Schedule widget added | - | Add widget to home | Widget shows | P2 | UI |
| TC-WD02 | Streak widget | Streak > 0 | Add widget | Streak count shown | P2 | UI |
| TC-WD03 | Quick review widget | Due cards | Add widget | Due count shown | P2 | UI |
| TC-WD04 | Countdown widget | Goal with date | Add widget | Days shown | P2 | UI |
| TC-WD05 | Widget tap - schedule | Schedule widget | Tap activity | Opens timetable | P2 | UI |
| TC-WD06 | Widget tap - review | Quick review | Tap | Opens Daily Review | P2 | UI |

---

## 33. Database Migrations

| ID | Test Case | Preconditions | Steps | Expected Result | Prio | Type |
|----|-----------|---------------|-------|-----------------|------|------|
| TC-DB01 | Migration 7 to 8 (SRS columns) | DB version 7 | Upgrade to 8 | qa_bank has SRS columns | P0 | Integration |
| TC-DB02 | Migration 8 to 9 (Pomodoro) | DB version 8 | Upgrade to 9 | pomodoro_sessions table | P0 | Integration |
| TC-DB03 | Migration 9 to 10 (timeSpentSeconds) | DB version 9 | Upgrade to 10 | attempt_answers has column | P0 | Integration |
| TC-DB04 | Migration 10 to 11 (chat_messages) | DB version 10 | Upgrade to 11 | chat_messages table | P0 | Integration |
| TC-DB05 | Fresh install | - | Install, open | DB version 11, no crash | P0 | Integration |

---

## 34. Error Handling & Edge Cases

| ID | Test Case | Preconditions | Steps | Expected Result | Prio | Type |
|----|-----------|---------------|-------|-----------------|------|------|
| TC-ER01 | Network error - Gemini | No network | Call Explain | Error shown, no crash | P0 | UI |
| TC-ER02 | Invalid API key | Wrong key | Test API key | Error message | P0 | UI |
| TC-ER03 | Empty assessment run | Assessment 0 questions | Start run | Handled gracefully | P0 | UI |
| TC-ER04 | Delete goal with assessments | Goal has assessments | Delete goal | Cascade or prompt | P0 | UI |
| TC-ER05 | Restore corrupt backup | Invalid JSON | Restore | Error, no crash | P0 | Integration |
| TC-ER06 | OCR no text | Image with no text | Extract | "No text found" message | P1 | UI |
| TC-ER07 | Speech recognition error | Mic denied | Use voice answer | Error message | P1 | UI |

---

## 35. License Repository (Freemium)

| ID | Test Case | Preconditions | Steps | Expected Result | Prio | Type |
|----|-----------|---------------|-------|-----------------|------|------|
| TC-LC01 | isPremium default | - | Call isPremium() | true (currently) | P1 | Unit |
| TC-LC02 | maxTimetables | - | Call maxTimetables() | Int.MAX_VALUE when premium | P1 | Unit |
| TC-LC03 | maxQAScansPerMonth | - | Call maxQAScansPerMonth() | Int.MAX_VALUE when premium | P1 | Unit |

---

## Summary Counts

| Category | Unit | Integration | UI | Total |
|----------|------|-------------|-----|-------|
| Utilities | 16 | 0 | 0 | 16 |
| SM-2 | 8 | 0 | 0 | 8 |
| Grading | 12 | 1 | 0 | 13 |
| Q&A Parser | 6 | 0 | 0 | 6 |
| Focus Guard | 6 | 0 | 0 | 6 |
| Backup | 0 | 8 | 0 | 8 |
| Share | 2 | 3 | 0 | 5 |
| Repositories | 0 | 60+ | 0 | 60+ |
| Templates | 4 | 0 | 0 | 4 |
| UI Flows | 0 | 0 | 90+ | 90+ |
| Migrations | 0 | 5 | 0 | 5 |
| Error handling | 0 | 1 | 6 | 7 |
| License | 3 | 0 | 0 | 3 |
| **Total** | **~51** | **~79** | **~96** | **~226** |

---

*Use this document to track test coverage and implement missing test cases.*
