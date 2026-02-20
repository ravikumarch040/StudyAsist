# StudyAsist Android

Kotlin + Jetpack Compose + Material 3. Includes app and Wear OS companion.

## Build

```bash
gradlew.bat assembleDebug   # Windows
./gradlew assembleDebug     # Linux/macOS
```

**If gradlew fails with "Unable to access jarfile gradle-wrapper.jar":**
1. Open this folder in **Android Studio** – it will regenerate the wrapper, or
2. Install Gradle and run: `gradle wrapper --gradle-version 8.9`

## Tests

```bash
gradlew.bat testDebugUnitTest
gradlew.bat connectedDebugAndroidTest   # requires device/emulator
```
