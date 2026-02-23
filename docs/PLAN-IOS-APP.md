# Plan: Native iOS App (Swift/SwiftUI) for StudyAsist

This document provides implementation details and reference for the StudyAsist iOS app. See the main plan in `.cursor/plans/` for the full roadmap.

## Tech Stack

| Layer | Choice |
|-------|--------|
| Language | Swift 5.9+ |
| UI | SwiftUI |
| Architecture | MVVM + Repository |
| Local DB | SwiftData (iOS 17+) |
| Networking | URLSession + async/await |
| Min iOS | 17.0 |
| Auth | Sign in with Apple, Google Sign-In (optional) |

## Project Structure

```
StudyAsist-iOS/
├── StudyAsist.xcodeproj
├── StudyAsist/
│   ├── App/
│   │   ├── StudyAsistApp.swift
│   │   ├── ContentView.swift
│   │   ├── AppState.swift
│   │   └── MainTabView.swift
│   ├── Models/
│   │   ├── Timetable, Activity, Goal, GoalItem, QA
│   │   ├── Assessment, AssessmentQuestion, Attempt, AttemptAnswer
│   │   ├── Result, BadgeEarned, StudyToolHistory
│   │   └── BackupData.swift (DTOs for sync)
│   ├── Data/Services/
│   │   ├── ApiClient.swift
│   │   ├── AuthService.swift
│   │   ├── KeychainService.swift
│   │   ├── BackupService.swift
│   │   ├── NotificationScheduler.swift
│   │   └── SettingsStore.swift
│   ├── Views/
│   │   ├── Timetable/
│   │   └── Settings/
│   └── Resources/
└── README.md
```

## Backend API

- Base URL: Set via `STUDYASIST_BACKEND_URL` or in Settings
- Auth: `POST api/auth/apple`, `POST api/auth/google` with id_token
- Sync: `POST api/sync/upload`, `GET api/sync/download` (BackupData payload)
- Leaderboard: `POST api/leaderboard/submit`, `GET api/leaderboard/top`
- Share: `POST api/share/create`, `GET api/share/resolve/{code}`

## Sync Format

BackupData JSON matches Android BackupRepository structure for cross-platform sync parity.

## Implementation Status

- **Phase 1**: Project setup, data layer, timetables, activities, settings, auth, sync, notifications
- **Phase 2**: Goals, Q&A Bank (with Vision OCR scan), Assessments, Results
- **Phase 3**: Study Hub (Dictate, Explain, Solve, Pomodoro)
- **Phase 4**: HealthKit, RevenueCat, widgets, localization (planned)
