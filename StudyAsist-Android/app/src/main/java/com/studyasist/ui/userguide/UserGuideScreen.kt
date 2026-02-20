package com.studyasist.ui.userguide

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.studyasist.R
import com.studyasist.ui.components.MarkdownText

private const val USER_GUIDE_CONTENT = """
# StudyAsist – Features & User Guide

Your AI-powered study companion. **Scan → Practice → Track → Improve**

---

## 1. Dashboard (Home)

- **Greeting** – Time-based (morning/afternoon/evening) with your name
- **Current Task** – Active timetable activity now or "Next up"
- **Goal Progress** – Circular progress % and days until exam
- **Last Result** – Most recent assessment score
- **Quick Actions** – Dictate, Explain, Solve, Assessments
- **Streak & Badges** – Study streak counter and earned badges
- **Today's Schedule** – All activities with current highlight

## 2. Timetables

- **Timetable List** – Create, duplicate, delete; set active
- **Active Timetable** – Drives Today, reminders, focus guard
- **Day View** – Single-day schedule in time order
- **Week View** – 7-column Mon–Sun grid
- **Timeline View** – Vertical timeline with pulsing current activity
- **Filter by Type** – Study, Break, School, Tuition, Sleep
- **Weekly Summary** – Hours per activity type
- **Copy from Day** – Duplicate day's schedule
- **Activity Types** – Distinct colors per type

## 3. Activities

- **Add/Edit** – Day, start/end, title, type, note
- **Notify Me** – Reminder 5–60 min before
- **Alarm Message** – Custom TTS: "Hey [name], time for [activity]"
- **Use Speech Sound** – Sound with spoken reminder
- **Block Overlap** – Prevent overlapping activities (Settings)
- **Copy Schedule** – Duplicate one day to another

## 4. Study Tools

**Dictate** – Photo → OCR → read aloud; language select
**Explain** – Text/image → AI explanation
**Solve** – Problem text/image → step-by-step solution

## 5. Q&A Bank

- **Scan Q&A** – Photo; OCR or AI extracts questions
- **Improve with AI** – Better extraction (Settings)
- **Manual Entry** – Add question, answer, type (MCQ, Short, Numeric, T/F, Essay)
- **Filter** – By subject and chapter
- **Create Assessment** – Build test from filtered Q&A
- **View Assessments/Results** – Quick links

## 6. Exam Goals

- **Add/Edit Goal** – Name, date, subjects/chapters, target hours
- **Goal Detail** – Days left, coverage %, score trend
- **On-Track / Behind** – Pace prediction
- **Suggested Practice** – Weak topics from recent attempts
- **Personal Bests** – Top 5 scores
- **Create Assessment** – Linked to goal
- **Add to Timetable** – Revision block
- **Quick Practice** – Start assessment

## 7. Assessments

- **Create** – Title, select by goal/subject/chapter/manual
- **Select Questions** – From Q&A bank with filters
- **Randomize** – Shuffle each attempt
- **Time Limit** – Set minutes
- **Types** – MCQ, short, numeric, T/F, essay
- **Run** – Timed; Voice or Image answer options
- **Edit/Delete** – Change or remove assessment

## 8. Results

- **Score** – Correct/max, percentage
- **Per-Question** – Your answer vs correct; partial credit
- **AI Grading** – Essays (Settings)
- **Revise** – Jump to Q&A by subject/chapter
- **Add to Timetable** – Revision for weak topics
- **Manual Review** – Flag, override score, feedback
- **Retry** – Same, all, or weak only
- **Export** – PDF, CSV, Excel, share as image
- **Print** – Via system dialog

## 9. Results List & Manual Review

- **Sort** – Newest, Oldest, Best, Lowest
- **Filter** – By assessment
- **Manual Review** – Override scores, add feedback

## 10. Spaced Repetition & Flashcards

**Daily Review** – SM-2 scheduled cards; tap flip; rate Again/Hard/Good/Easy
**Flashcards** – Swipe right=correct, left=wrong; tap to flip; filter by subject/chapter

## 11. Pomodoro Timer

- **Focus** – 25/45/50 min configurable
- **Short/Long Break** – 5–15 min / 15–30 min
- **Circular Countdown** – Animated arc
- **Start/Pause/Resume/Skip/Reset**
- **Session Counter** – "Session 3 of 4"
- **Total Focus Today** – Sum of completed minutes
- **Subject Tagging** – Optional per session

## 12. AI Features

- **Study Plan** – Goal name, subjects, exam date → AI plan
- **AI Tutor Chat** – Ask anything; context-aware
- **Q&A Parsing** – Improve with AI (Settings)
- **Subjective Grading** – Essays via Gemini (Settings)
- **API Key** – Required in Settings; test connection

## 13. Add Revision

- **Subject & Chapter** – Pre-filled from result/goal
- **Select Timetable** – Add block to chosen timetable
- **Day & Time** – Pick slot

## 14. Notifications & Focus Guard

- **Reminders** – Before activities (5–60 min)
- **Vibration** – Toggle
- **Exam Alerts** – When exam near and coverage low
- **Focus Guard** – Alerts when opening games/social during study; custom app list

## 15. Settings

**Profile** – Photo, name
**Appearance** – Language (EN, HI, ES, FR, DE), Dark mode, 11 themes
**Notifications** – Lead time, vibration, block overlap
**Exam Alerts** – Days & coverage thresholds
**Focus Guard** – Enable, built-in list, custom apps
**Speech** – TTS voice for alarms
**AI** – Gemini API key, test
**Privacy** – Use AI for parsing, grading
**Cloud Backup** – Folder or Drive; manual/auto; restore
**Backup/Restore** – Export/import JSON
**Exam Data** – Export/import goals+Q&A+assessments only

## 16. Export & Share

- **Timetable** – CSV, PDF, Excel, image, print
- **Results** – PDF, CSV, Excel, image, print
- **Share Assessment** – Code for friend to take same test

## 17. Gamification & Onboarding

- **Streak** – Consecutive days with assessments
- **Badges** – First Step, Week Warrior, Monthly Master, Perfect, Dedicated, Century, Pomodoro, etc.
- **Onboarding** – 4-page intro; enter name; Get Started

## 18. Widgets

- **Schedule** – Today's activities
- **Streak** – Study streak count
- **Quick Review** – Due cards; tap to review
- **Countdown** – Days until exam

## 19. Navigation

- **Bottom Nav** – Home, Timetable, Study, Goals, More
- **Study Hub** – Dictate, Explain, Solve, Pomodoro
- **Goals Hub** – Goals, Q&A Bank, Assessments, Results, Daily Review, Flashcards, Manual Review
- **More Hub** – Leaderboard, Online Resources, Downloaded Docs, Settings

---

*Scan → Practice → Track → Improve*
"""

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserGuideScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.user_guide)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            MarkdownText(
                text = USER_GUIDE_CONTENT,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
