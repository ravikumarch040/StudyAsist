# Credential Manager Migration for Google Drive

## Status: Deferred

The current Google Drive backup uses deprecated `GoogleSignIn` and `GoogleAccountCredential`. Full migration to Credential Manager is complex.

## Key Constraint

**Credential Manager provides authentication (ID token), not authorization (OAuth2 access token).** The Drive API requires OAuth2 access tokens with `drive.file` scope. `GetGoogleIdOption` / `GoogleIdTokenCredential` from Credential Manager return ID tokens only—no access token for Drive.

## Options

1. **Hybrid**: Use Credential Manager for backend auth (ID token → JWT exchange) and keep Google Sign-In for Drive backup when `cloudBackupTarget == "google_drive"`. Splits flows but works.
2. **Full migration**: Use [Google Identity Authorization for Android](https://developers.google.com/identity/authorization/android) for Drive OAuth2 scopes—separate from Credential Manager.
3. **Backend proxy**: Have backend exchange ID token for Drive access token; app never uses Drive API directly.

## Completed

- [x] Add `androidx.credentials:credentials` and `credentials-play-services-auth` (done).

## Pending (when migrating)

1. For **backend auth only**: Replace `GoogleSignIn` sign-in intent with `CredentialManager.getCredential()` + `GetGoogleIdOption`; use ID token for `authRepository.loginWithGoogle()`.
2. For **Drive backup**: Either keep Google Sign-In for Drive scope, or implement Authorization API flow for access tokens.
3. Handle sign-out: `CredentialManager.clearCredentialState()` for Credential Manager credentials.
4. Remove `@Suppress("DEPRECATION")` where no longer needed.

## References

- [Credential Manager](https://developer.android.com/training/sign-in/credential-manager)
- [Legacy GSI Migration](https://developer.android.com/training/sign-in/legacy-gsi-migration)
- [Google Identity Authorization (access tokens)](https://developers.google.com/identity/authorization/android)
