# Credential Manager Migration for Google Drive

## Status: Hybrid Implemented

**Backend auth** now uses Credential Manager. **Drive backup** still uses Google Sign-In (needs OAuth2 access tokens).

## Key Constraint

**Credential Manager provides authentication (ID token), not authorization (OAuth2 access token).** The Drive API requires access tokens; Credential Manager returns ID tokens only.

## Implemented (Hybrid)

- [x] **CredentialManagerAuthHelper**: `getGoogleIdToken(Activity)` via Credential Manager
- [x] **Settings Account section**: "Sign in with Google" uses Credential Manager when `isBackendAuthConfigured`
- [x] **Onboarding Account page**: Same Credential Manager flow
- [x] **Drive backup**: Still uses Google Sign-In intent (unchanged)
- [x] Dependencies: `credentials`, `credentials-play-services-auth`, `googleid:1.1.1`

## Pending (when migrating)

1. For **backend auth only**: Replace `GoogleSignIn` sign-in intent with `CredentialManager.getCredential()` + `GetGoogleIdOption`; use ID token for `authRepository.loginWithGoogle()`.
2. For **Drive backup**: Either keep Google Sign-In for Drive scope, or implement Authorization API flow for access tokens.
3. Handle sign-out: `CredentialManager.clearCredentialState()` for Credential Manager credentials.
4. Remove `@Suppress("DEPRECATION")` where no longer needed.

## References

- [Credential Manager](https://developer.android.com/training/sign-in/credential-manager)
- [Legacy GSI Migration](https://developer.android.com/training/sign-in/legacy-gsi-migration)
- [Google Identity Authorization (access tokens)](https://developers.google.com/identity/authorization/android)
