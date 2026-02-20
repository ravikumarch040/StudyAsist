# StudyAsist

**The complete AI-powered study companion for students.**

Scan your textbooks, build a Q&A bank, practice with flashcards, track progress with spaced repetition, and ace your exams — all in one app.

**Scan → Practice → Track → Improve**

---

## Workspace Structure

| Folder | Description |
|--------|-------------|
| **StudyAsist-Android** | Android app (Kotlin + Jetpack Compose + Wear OS) |
| **StudyAsist-Backend** | Python FastAPI backend (auth, sync, leaderboards) |
| **StudyAsist-iOS** | Placeholder for future native iOS app |

**Open Android project:** Use Android Studio to open the `StudyAsist-Android` folder directly.

---

## Quick Start

### Android

**First time setup:** If `gradlew` fails with "Unable to access jarfile gradle-wrapper.jar", either:
- Open `StudyAsist-Android` in **Android Studio** – it will auto-generate the wrapper, or
- Run `gradle wrapper --gradle-version 8.9` from `StudyAsist-Android` if Gradle is installed.

```bash
cd StudyAsist-Android
gradlew.bat assembleDebug   # Windows
./gradlew assembleDebug    # Linux/macOS
```

### Backend

```bash
cd StudyAsist-Backend
pip install -r requirements.txt
uvicorn main:app --reload
```

API: http://localhost:8000 | Docs: http://localhost:8000/docs

### iOS

See [StudyAsist-iOS/README.md](StudyAsist-iOS/README.md) – implementation planned separately.

---

## Documentation

- [docs/FEATURES_AND_USER_GUIDE.md](docs/FEATURES_AND_USER_GUIDE.md) – Features and user guide
- [docs/DESIGN.md](docs/DESIGN.md) – Design and architecture
- [docs/TEST_CASES_SUITE.md](docs/TEST_CASES_SUITE.md) – Test cases
- [StudyAsist-Backend/README.md](StudyAsist-Backend/README.md) – Backend API
