# StudyAsist iOS

Native iOS app (Swift/SwiftUI) for StudyAsist – AI-powered study companion.

## Requirements

- Xcode 15+
- iOS 17.0+
- macOS for building and running

## Setup

1. **Clone and open**
   ```bash
   cd StudyAsist-iOS
   open StudyAsist.xcodeproj
   ```

2. **Backend URL**
   - Set `STUDYASIST_BACKEND_URL` in your Scheme's Environment Variables, or
   - Use Settings in the app (stored in UserDefaults)

3. **Sign in with Apple**
   - Requires Apple Developer account
   - Capability is enabled via `StudyAsist.entitlements`
   - Backend must have `APPLE_BUNDLE_ID` configured to match `com.studyasist`

4. **Run**
   - Select a simulator or device
   - Build and run (Cmd+R)

## Implemented

- **Timetables**: Create, duplicate, delete; Day/Week view; filter by activity type
- **Activities**: Add/edit with overlap warning; reminders (notifications)
- **Goals**: Add/edit goals with subjects and chapters; goal detail with days remaining
- **Q&A Bank**: List, filter by subject/chapter; add manually or scan (camera/gallery) with Vision OCR
- **Assessments**: Create from Q&A bank; run timed; simple text-answer grading
- **Results**: List and detail with per-question breakdown
- **Study Tools**: Dictate (OCR + TTS), Explain, Solve (Gemini API), Pomodoro timer
- **Settings**: Profile, notifications, AI (Gemini key), backup & restore, sync (when signed in)
- **Auth**: Sign in with Apple; JWT in Keychain
- **Sync**: Upload/download to StudyAsist-Backend when signed in
- **Backup**: Export/import JSON (compatible with Android backup format)

## Planned (Phase 4)

- HealthKit (sleep data), RevenueCat monetization, Widgets
- Full localization (Hindi, Spanish, French, German)
- Onboarding flow

## Structure

- `StudyAsist/App/` – App entry, ContentView, MainTabView
- `StudyAsist/Models/` – SwiftData models, BackupData DTOs
- `StudyAsist/Data/Services/` – ApiClient, Auth, Backup, Notifications
- `StudyAsist/Views/` – Screens (Timetable, Settings)
