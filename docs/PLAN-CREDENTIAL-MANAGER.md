# Credential Manager Migration for Google Drive

## Status: Planned

The current Google Drive backup uses deprecated `GoogleSignIn` and `GoogleAccountCredential`. Migration to Credential Manager is planned.

## Steps

1. Add `androidx.credentials:credentials` and `credentials-play-services-auth` (done).
2. Replace `GoogleSignIn` with `CredentialManager.getCredential()` requesting `GoogleIdTokenRequest`.
3. Use the ID token or access token to authenticate Drive API calls.
4. Handle sign-out via `CredentialManager.clearCredentialState()`.
5. Remove `@Suppress("DEPRECATION")` from `DriveApiBackupProvider` and `SettingsViewModel`.

## References

- [Credential Manager](https://developer.android.com/training/sign-in/credential-manager)
- [Google ID token with Credential Manager](https://developers.google.com/identity/android/credential-manager)
