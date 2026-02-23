# Credential Manager Migration for Google Drive

## Status: Hybrid Implemented (Drive Migration Deferred)

**Backend auth** now uses Credential Manager. **Drive backup** still uses Google Sign-In (needs OAuth2 access tokens).

## Decision: Drive Keeps Google Sign-In

**Credential Manager provides authentication (ID token), not authorization (OAuth2 access token).** The Drive API requires access tokens. Migration options:

1. **Keep Google Sign-In for Drive** (current): Works; no backend needed. `@Suppress("DEPRECATION")` retained in `DriveApiBackupProvider.kt` and `SettingsViewModel.kt`.
2. **Authorization API**: Use `AuthorizationClient` from Google Identity to obtain access tokens. Requires OAuth client setup and possibly a thin backend for token exchange. See [Google Identity Authorization](https://developers.google.com/identity/authorization/android).

**Chosen**: Option 1 until Authorization API is fully integrated.

## Implemented (Hybrid)

- [x] **CredentialManagerAuthHelper**: `getGoogleIdToken(Activity)` via Credential Manager
- [x] **Settings Account section**: "Sign in with Google" uses Credential Manager when `isBackendAuthConfigured`
- [x] **Onboarding Account page**: Same Credential Manager flow
- [x] **Drive backup**: Uses Google Sign-In intent (unchanged; intentional per above)
- [x] Dependencies: `credentials`, `credentials-play-services-auth`, `googleid:1.1.1`

## Implemented (additional)

- [x] **Sign-out**: `CredentialManagerAuthHelper.clearCredentialState()` called on account sign-out when backend auth is configured

## Deferred (Drive Migration)

1. ~~For **backend auth only**: Replace GoogleSignIn…~~ Backend auth uses Credential Manager.
2. **For Drive backup**: Deferred. See "Decision" above.
3. ~~Handle sign-out~~ Done.
4. **Remove `@Suppress("DEPRECATION")`**: Deferred for Drive-related files until Option 2 is implemented.

## References

- [Credential Manager](https://developer.android.com/training/sign-in/credential-manager)
- [Legacy GSI Migration](https://developer.android.com/training/sign-in/legacy-gsi-migration)
- [Google Identity Authorization (access tokens)](https://developers.google.com/identity/authorization/android)
