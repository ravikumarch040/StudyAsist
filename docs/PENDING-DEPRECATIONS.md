# Pending Deprecation Migrations

List of `@Suppress("DEPRECATION")` usages and migration paths.

## Drive / Google Sign-In

| File | Reason | Migration |
|------|--------|-----------|
| `DriveApiBackupProvider.kt` | GoogleSignIn, GoogleAccountCredential | See PLAN-CREDENTIAL-MANAGER. Deferred: Drive needs OAuth2 access tokens. |
| `SettingsViewModel.kt` | Google Sign-In intent for Drive | Same as above. |

## Camera / Image Picker

| File | Reason | Migration |
|------|--------|-----------|
| `QAScanScreen.kt` | TakePicturePreview / legacy camera | Migrate to CameraX `ImageCapture` + `ImageAnalysis` |
| `DictateScreen.kt` | TakePicture contract | Consider CameraX or keep if API still supported |
| `ExplainScreen.kt` | Image picker / camera | Same |
| `SolveScreen.kt` | Same | Same |
| `AssessmentCreateScreen.kt` | Same | Same |

## Other

| File | Reason | Migration |
|------|--------|-----------|
| `SettingsScreen.kt` | Various deprecated APIs (e.g. ActivityResultContracts) | Review each; some may be replaced by newer Compose/Activity APIs |
| `Theme.kt` | ColorScheme/typography API | Update to latest Material3 APIs |
| `FocusGuardHelper.kt` | UsageStatsManager / AppOps | Check Android docs for replacement |
| `ReminderAlarmActivity.kt` | Alarm / PendingIntent | Review AlarmManager best practices |

## Priority

1. **Drive**: Documented; keep until Authorization API migration.
2. **Camera**: Low priority if current APIs work; migrate when CameraX is standard.
3. **Theme/FocusGuard/ReminderAlarm**: Review per Android version targets.
