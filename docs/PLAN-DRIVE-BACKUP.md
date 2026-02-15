# Google Drive API – Direct Backup (Optional)

**Status:** Planned  
**Prerequisite:** Current cloud backup works via DocumentsProvider (Drive, Dropbox, etc.). This adds an optional direct Google Drive API path.

---

## 1. Goal

Offer a **direct Google Drive** backup option that:
- Uses Google Sign-In for auth (no manual folder picker)
- Uploads backups to a dedicated app folder (e.g. `StudyAsist/`)
- Supports the same manual and scheduled backup flows
- Keeps the existing folder-based backup as default

---

## 2. Architecture

```
Settings
  ├── Cloud backup target: "Folder" (current) | "Google Drive"
  ├── [Folder] DocumentsProvider URI (when target = Folder)
  └── [Drive] Account + app folder (when target = Drive)

CloudBackupWorker
  ├── if (target == Folder) → use DocumentsContract (current)
  └── if (target == Drive)  → use DriveApiBackupProvider
```

---

## 3. Dependencies

| Library | Purpose |
|---------|---------|
| `play-services-auth` | Google Sign-In, credentials |
| `google-api-services-drive` | Drive REST API v3 client |

---

## 4. Implementation Steps

### Phase 1 – Setup
1. Add `play-services-auth` and `google-api-services-drive` to `build.gradle.kts`.
2. Create `CloudBackupTarget` enum: `FOLDER`, `GOOGLE_DRIVE`.
3. Add `cloudBackupTarget` to `SettingsDataStore` and `AppSettings`.
4. Add `DriveApiBackupProvider` interface.

### Phase 2 – Drive integration
5. Enable Google Drive API in GCP console.
6. Add OAuth 2.0 client (Android) in GCP.
7. Implement `DriveApiBackupProvider` using `GoogleSignIn` + `Drive.Files.create()`.
8. Create or reuse app folder (e.g. `StudyAsist`) in Drive.

### Phase 3 – UI & scheduling
9. Settings: radio/chips for "Folder" vs "Google Drive".
10. When Drive: show "Sign in with Google" and backup status.
11. Reuse `CloudBackupWorker`; inject provider based on target.
12. Include `cloudBackupTarget` in backup/restore JSON.

---

## 5. Scopes

- `DriveScopes.DRIVE_FILE` – access only to files created by the app (or `DRIVE_APPDATA` for app data folder).

---

## 6. Risks

| Risk | Mitigation |
|------|------------|
| OAuth setup in GCP | Document steps in README / developer docs |
| Play Services on device | Fall back to folder backup if unavailable |
| API key / config | Use BuildConfig or secure storage; never commit secrets |

---

## 7. File checklist

| Layer | Action | File |
|-------|--------|------|
| Config | Add | `google-services.json` (if using Firebase) or `credentials.json` reference |
| DataStore | Modify | `SettingsDataStore.kt` – add `cloudBackupTarget` |
| Repository | Create | `DriveApiBackupProvider.kt` |
| Worker | Modify | `CloudBackupWorker.kt` – branch by target |
| UI | Modify | `SettingsScreen.kt` – target selector + Drive sign-in |
| Backup | Modify | `BackupRepository.kt` – include `cloudBackupTarget` in export |
