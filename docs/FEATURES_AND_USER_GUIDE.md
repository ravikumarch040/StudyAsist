# StudyAsist – Complete Features & User Guide

A comprehensive list of all features and sub-features in StudyAsist, your AI-powered study companion.

---

## 1. Dashboard (Home)

**Overview:** Your daily study hub showing current tasks, progress, and quick access to everything.

| Feature | Description |
|---------|-------------|
| **Greeting** | Time-based greeting (Good morning/afternoon/evening) with your name |
| **Current Task** | Shows the active activity from your timetable right now, or "Next up" if between activities |
| **Goal Progress** | Circular progress ring for your active exam goal with percentage and days remaining |
| **Last Result** | Quick view of your most recent assessment score with tap to see details |
| **Quick Actions** | One-tap shortcuts: Dictate, Explain, Solve, Assessments |
| **Streak & Badges** | Study streak counter and earned badges (First Step, Week Warrior, etc.) |
| **Today's Schedule** | Scrollable list of today's activities with current-activity highlight |

---

## 2. Timetables

**Overview:** Organize your study schedule with daily and weekly views.

| Feature | Description |
|---------|-------------|
| **Timetable List** | Create, duplicate, delete timetables; set one as active |
| **Active Timetable** | Only the active timetable drives Today, reminders, and focus guard |
| **Day View** | Single-day schedule with all activities in time order |
| **Week View** | 7-column grid showing Mon–Sun; tap cell to add activity |
| **Timeline View** | Vertical timeline with connected dots; current activity pulses |
| **Filter by Type** | Filter activities by Study, Break, School, Tuition, Sleep |
| **Weekly Summary** | Total hours per activity type (e.g., Study 12h, Break 3h) |
| **Copy from Day** | Copy an entire day's schedule to another day |
| **Activity Types** | Study, Break, School, Tuition, Sleep with distinct colors |

---

## 3. Activity Management

| Feature | Description |
|---------|-------------|
| **Add Activity** | Day, start/end time, title, type, note; optional notification |
| **Edit Activity** | Modify any activity; overlap warning when times clash |
| **Notify Me** | Reminder X minutes before activity (5, 10, 15, 30, 60 min) |
| **Alarm Message** | Custom TTS message when reminder fires (e.g., "Hey [name], it's time for [activity]") |
| **Use Speech Sound** | Play a sound with the spoken reminder |
| **Block Overlap** | Prevent saving when another activity overlaps (Settings toggle) |
| **Copy Schedule** | Duplicate one day's schedule to another |

---

## 4. Study Tools

### 4.1 Dictate (Read Aloud)

| Feature | Description |
|---------|-------------|
| **Take Photo** | Capture text from camera |
| **Upload Image** | Select from gallery |
| **OCR Extraction** | ML Kit reads text from image |
| **Language for Reading** | Choose reading language (e.g., English, Hindi) |
| **Read Aloud** | TTS reads extracted text; Stop button to cancel |
| **Edit Text** | Edit extracted text before reading if OCR misreads |

### 4.2 Explain

| Feature | Description |
|---------|-------------|
| **Text Input** | Type or paste text to explain |
| **Image Input** | Upload/capture image; text extracted first, then explained |
| **AI Explanation** | Gemini API provides clear explanation |
| **Language Option** | Choose explanation language |

### 4.3 Solve

| Feature | Description |
|---------|-------------|
| **Problem Input** | Type or paste math/problem |
| **Image Input** | Capture problem from photo; AI extracts and solves |
| **Step-by-Step Solution** | AI shows working solution |
| **Language Option** | Choose solution language |

---

## 5. Q&A Bank

**Overview:** Build and manage your question bank from scanned or typed content.

| Feature | Description |
|---------|-------------|
| **Scan Q&A** | Take photo or choose image; extract questions automatically |
| **Improve with AI** | Gemini improves/extracts Q&A from rough OCR (Settings: use cloud for parsing) |
| **Manual Entry** | Add row: question, answer, type (MCQ, Short, Numeric, True/False, Essay) |
| **Filter by Subject** | Dropdown to filter by subject |
| **Filter by Chapter** | Dropdown to filter by chapter |
| **Delete Q&A** | Remove individual items |
| **Create Assessment** | Build practice test from filtered Q&A |
| **View Assessments** | Quick link to assessment list |
| **View Results** | Quick link to result history |

---

## 6. Exam Goals

| Feature | Description |
|---------|-------------|
| **Add Goal** | Name, exam date, subjects/chapters, optional target hours |
| **Edit Goal** | Modify goal details |
| **Goal Detail** | Days remaining, coverage %, score trend, suggested practice |
| **On-Track / Behind** | Prediction based on current pace vs target |
| **Suggested Practice** | Topics with wrong/partial answers in recent attempts |
| **Personal Bests** | Top 5 highest scores |
| **Create Assessment** | Create assessment linked to this goal |
| **Add to Timetable** | Add revision block for subject/chapter |
| **Quick Practice** | Start assessment for this goal |

---

## 7. Assessments

| Feature | Description |
|---------|-------------|
| **Create Assessment** | Title, select by goal, subject/chapter, or manual; add questions from Q&A bank |
| **Select Questions** | Pick from Q&A bank; filter by subject/chapter |
| **Randomize Questions** | Shuffle order each attempt |
| **Time Limit** | Set total time in minutes |
| **Question Types** | MCQ, short answer, numeric, true/false, essay |
| **Run Assessment** | Timed run with Next/Previous; voice or image answer options |
| **Voice Answer** | Use speech recognition to answer |
| **Image Answer** | Take photo; OCR or AI grades (for math, etc.) |
| **Edit Assessment** | Change title, questions, time |
| **Delete Assessment** | Remove assessment (confirm dialog) |

---

## 8. Assessment Results

| Feature | Description |
|---------|-------------|
| **Score Display** | Correct/max, percentage |
| **Per-Question Breakdown** | Your answer vs correct; partial credit for essays |
| **AI Grading** | Subjective grading for essays (Settings: use cloud for grading) |
| **Leaderboard Submit** | Score submitted to leaderboard when signed in (automatic) |
| **Revise** | Jump to Q&A bank filtered by subject/chapter |
| **Add to Timetable** | Add revision block for weak topics |
| **Manual Review** | Flag answers, override scores, add feedback |
| **Retry** | Retry same assessment, all questions, or weak only |
| **Export** | PDF, CSV, Excel, share as image |
| **Print** | Print result via system dialog |

---

## 9. Results List

| Feature | Description |
|---------|-------------|
| **Sort** | Newest, Oldest, Best score, Lowest score |
| **Filter by Assessment** | Show only attempts of selected assessment |
| **Tap to View** | Open full result detail |
| **Manual Review** | Link to items needing manual override |

---

## 10. Manual Review

| Feature | Description |
|---------|-------------|
| **Review List** | All flagged or override-worthy attempts |
| **Override Score** | Change score per question |
| **Manual Feedback** | Add text feedback for partial/incorrect |
| **Save** | Apply overrides and close |

---

## 11. Spaced Repetition & Flashcards

### 11.1 Daily Review (SRS)

| Feature | Description |
|---------|-------------|
| **Due Cards** | Cards scheduled by SM-2 algorithm for today |
| **Card Flip** | Tap to reveal answer |
| **Rate** | Again, Hard, Good, Easy – updates next review date |
| **Progress Bar** | Reviewed vs total in session |
| **Session Summary** | Cards reviewed, accuracy, time spent at end |

### 11.2 Flashcards

| Feature | Description |
|---------|-------------|
| **Swipe Right** | Correct (Good) |
| **Swipe Left** | Wrong (Again) |
| **Tap to Flip** | Show/hide answer |
| **Filter** | By subject/chapter chips |
| **Quick Review** | Load limited cards for short session |
| **Session Complete** | Summary when done |

---

## 12. Pomodoro Timer

| Feature | Description |
|---------|-------------|
| **Focus Timer** | Configurable focus duration (25, 45, 50 min) |
| **Short Break** | 5, 10, or 15 min |
| **Long Break** | After 4 focus sessions |
| **Circular Countdown** | Animated arc showing remaining time |
| **Start / Pause / Resume** | Standard controls |
| **Skip** | Skip to next phase |
| **Reset** | Restart current phase |
| **Session Counter** | "Session 3 of 4" |
| **Total Focus Today** | Sum of completed focus minutes |
| **Subject Tagging** | Optional subject per session |
| **Settings** | Focus, short break, long break duration; auto-start breaks |

---

## 13. AI Features

### 13.1 AI Study Plan

| Feature | Description |
|---------|-------------|
| **Goal Name** | Enter exam or goal name |
| **Subjects** | Comma-separated subjects |
| **Exam Date** | Target date |
| **Generate** | AI creates personalized weekly/daily study plan (markdown) |
| **Markdown Display** | Formatted plan with headers, bullets |

### 13.2 AI Tutor Chat

| Feature | Description |
|---------|-------------|
| **Chat UI** | User messages on right, AI on left |
| **Send Message** | Ask anything about your studies |
| **Context-Aware** | AI uses recent wrong answers, subject, level |
| **Chat History** | Stored per session |
| **Loading Indicator** | While AI is responding |

### 13.3 AI in Q&A & Grading

| Feature | Description |
|---------|-------------|
| **Improve with AI** | Better Q&A extraction from scanned pages (Settings) |
| **Subjective Grading** | Essay/short answers graded by Gemini (Settings) |
| **Math Equivalence** | "1/2" vs "0.5" accepted as same |
| **Gemini API Key** | Required; add in Settings, test connection |

---

## 14. Add Revision (Timetable)

| Feature | Description |
|---------|-------------|
| **Subject & Chapter** | Pre-filled from assessment result or goal |
| **Select Timetable** | Choose which timetable to add block to |
| **Day & Time** | Pick day and time range |
| **Add** | Creates revision activity in timetable |

---

## 15. Notifications & Alerts

| Feature | Description |
|---------|-------------|
| **Study Reminder** | Notifications before timetable activities (active only) |
| **Default Lead Time** | 5, 10, 15, 30, 60 min before (Settings) |
| **Vibration** | Toggle vibration on reminders (Settings) |
| **Alarm Channel** | Ring-until-dismiss style for high-priority |
| **Custom TTS** | Spoken message with your name and activity |
| **Exam Goal Alerts** | When exam within X days AND coverage below Y% (Settings) |
| **Boot Reschedule** | Reminders rescheduled after device restart |

---

## 16. Focus Guard

| Feature | Description |
|---------|-------------|
| **Enable** | Alerts when opening games, YouTube, social apps during study blocks |
| **Usage Access** | Requires permission to detect foreground app |
| **Built-in List** | Games, YouTube, Instagram, etc. |
| **Custom Apps** | Add package names (e.g., com.example.game) |
| **Notification** | Gentle nudge; does not block apps |
| **Foreground Service** | Runs during study blocks only |

---

## 17. Settings

### 17.0 Account (Backend sign-in)

| Feature | Description |
|---------|-------------|
| **Sign in with Google** | Exchange Google id token for JWT; enables sync, leaderboard, backend share |
| **Sign in with Apple** | Web flow; enabled when `APPLE_SERVICE_ID` configured; backend callback redirects to app |
| **Sign out** | Clear JWT; local data retained |
| **Sync data** | Visible when signed in; Upload to cloud / Download from cloud |

### 17.1 Profile

| Feature | Description |
|---------|-------------|
| **Profile Photo** | Set avatar (editable) |
| **Your Name** | Used in greetings and TTS alarms |

### 17.2 Appearance

| Feature | Description |
|---------|-------------|
| **Language** | System, English, Hindi, Spanish, French, German |
| **Dark Mode** | System, Light, Dark |
| **Theme** | 11 themes: Dynamic, Minimal Light/Dark, Pastel Calm, Academic Paper, Vibrant Study, Dark High Contrast, Neo Glass, Retro Chalkboard, Nature Earthy, Productivity Dashboard |

### 17.3 Notifications

| Feature | Description |
|---------|-------------|
| **Default Reminder** | Minutes before activity (5–60) |
| **Vibration** | On/off |
| **Block Overlap** | Prevent overlapping activities |

### 17.4 Exam Goal Alerts

| Feature | Description |
|---------|-------------|
| **Days Threshold** | Alert when exam within 1, 3, 7, 14, 30 days |
| **Coverage Threshold** | Alert when below 25%, 50%, 75%, 100% |

### 17.5 Focus Guard

| Feature | Description |
|---------|-------------|
| **Enable** | Toggle on/off |
| **Built-in Apps** | View list of monitored apps |
| **Custom Apps** | Add/remove package names |

### 17.6 Speech (India Voices)

| Feature | Description |
|---------|-------------|
| **TTS Voice** | Choose voice for alarms and read-aloud |
| **System Default** | Or pick from India-region voices |

### 17.7 AI (Explain / Solve)

| Feature | Description |
|---------|-------------|
| **Gemini API Key** | Paste key from Google AI Studio |
| **Test API Key** | Verify connection |

### 17.8 AI & Privacy

| Feature | Description |
|---------|-------------|
| **Use AI for Q&A Parsing** | Enable "Improve with AI" in scan |
| **Use AI for Subjective Grading** | Enable LLM grading for essays |

### 17.9 Cloud Backup

| Feature | Description |
|---------|-------------|
| **Target** | Folder (Drive app, etc.) or Google Drive (direct) |
| **Set Backup Folder** | Pick folder for local/Drive app backup |
| **Sign in with Google** | For direct Drive; sign out option |
| **Backup to Cloud** | Manual backup now |
| **Restore from Backup** | Pick file from folder/Drive |
| **Auto Daily Backup** | Schedule daily automatic backup |

### 17.10 Backup & Restore

| Feature | Description |
|---------|-------------|
| **Backup** | Export all data to JSON file |
| **Restore** | Import from JSON backup |

### 17.11 Exam Data Only

| Feature | Description |
|---------|-------------|
| **Export Exam Data** | Goals, Q&A, assessments, attempts, results only |
| **Restore Exam Data** | Import exam data (replaces existing) |

### 17.12 Accessibility (when implemented)

| Feature | Description |
|---------|-------------|
| **Font Scale** | 0.85x, 1.0x, 1.15x, 1.3x |
| **Haptic Feedback** | Vibration on taps |
| **High Contrast** | Enhanced contrast mode |
| **Color-Blind Mode** | Blue/orange instead of red/green |

### 17.13 Pomodoro Settings

| Feature | Description |
|---------|-------------|
| **Focus Duration** | 25, 45, 50 min |
| **Short Break** | 5, 10, 15 min |
| **Long Break** | 15, 20, 30 min |
| **Auto-Start Breaks** | Automatically start break when focus ends |

---

## 18. Export & Share

| Feature | Description |
|---------|-------------|
| **Export Timetable** | CSV, PDF, Excel; share as image |
| **Print Timetable** | PDF via Print dialog |
| **Export Results** | PDF, CSV, Excel from result list/detail |
| **Share as Image** | Timetable or result as PNG |
| **Share Assessment** | Generate shareable code; when signed in uses backend (short alphanumeric code); otherwise local Base64 |

---

## 19. Gamification

| Feature | Description |
|---------|-------------|
| **Study Streak** | Consecutive days with completed assessments |
| **Badges** | First Step, Week Warrior, Monthly Master, Perfect, Dedicated, Century, First Review, Review Streak, Cards Mastered, First Pomodoro, Pomodoro Pro |
| **Streak Display** | On dashboard when > 0 |
| **Badge Display** | Earned badges on dashboard |

---

## 20. Onboarding

| Feature | Description |
|---------|-------------|
| **Welcome** | 4-page horizontal pager |
| **Pages** | Timetable, Scan Q&A, Assess, Goals |
| **Skip** | Skip to end |
| **Enter Name** | Final page asks your name |
| **Get Started** | Completes onboarding, navigates to Home |

---

## 21. Widgets

| Feature | Description |
|---------|-------------|
| **Schedule Widget** | Today's upcoming activities |
| **Streak Widget** | Study streak count |
| **Quick Review Widget** | Due cards count; tap to open Daily Review |
| **Countdown Widget** | Days until next exam |

---

## 22. Leaderboard

| Feature | Description |
|---------|-------------|
| **Leaderboard** | Top scores from backend; rank, user name, score/max, percentage, streak days |
| **Access** | More Hub → Leaderboard |
| **Sign in required** | Shows message when not signed in |

## 23. Navigation & Hub

| Feature | Description |
|---------|-------------|
| **Bottom Nav** | Home, Timetable, Study, Goals, More (5 tabs) |
| **Study Hub** | Dictate, Explain, Solve, Pomodoro, Q&A Bank, Assessments, Results, Daily Review, Flashcards |
| **More Hub** | Q&A Bank, Assessments, Results, Manual Review, **Leaderboard**, Settings |

---

## 24. Miscellaneous

| Feature | Description |
|---------|-------------|
| **Activity Colors** | Color psychology: Study (blue), Break (green), School/Tuition (amber), Sleep (indigo) |
| **Timeline View** | Vertical timeline in timetable detail |
| **PDF Import** | Extract Q&A from PDF pages (PdfRenderer + OCR) |
| **Timetable Templates** | Pre-built templates (e.g., Generic School Day, Exam Prep) |
| **Profile Avatar** | Editable profile picture in Settings |

---

*StudyAsist – Scan → Practice → Track → Improve*
