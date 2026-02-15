# Google Drive API Setup (GCP)

Follow these steps to enable direct Google Drive backup in StudyAsist.

---

## Step 1: Create or Select a Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/).
2. Create a new project or select an existing one.
3. Note the **Project ID**.

---

## Step 2: Enable Google Drive API

1. In the Cloud Console, go to **APIs & Services** → **Library**.
2. Search for **Google Drive API**.
3. Click **Enable**.

---

## Step 3: Create OAuth 2.0 Credentials (Android)

1. Go to **APIs & Services** → **Credentials**.
2. Click **Create Credentials** → **OAuth client ID**.
3. If prompted, configure the **OAuth consent screen**:
   - User type: **External** (or Internal for workspace)
   - App name: **StudyAsist**
   - Support email: your email
   - Scopes: Add `.../auth/drive.file` (Drive file access)
4. For the OAuth client:
   - Application type: **Android**
   - Name: **StudyAsist Android**
   - Package name: `com.studyasist`
   - SHA-1 certificate fingerprint: from your signing key (debug or release)

### Get SHA-1 Fingerprint

**Debug (development):**
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

**Release:**
```bash
keytool -list -v -keystore /path/to/your/release.keystore -alias your_alias
```

Copy the **SHA-1** value (e.g. `AA:BB:CC:...`) and paste it in the credential form.

5. Click **Create**. You will see the **Client ID** (not needed in app code – matched by package + SHA-1).

---

## Step 4: Verify

- Ensure **Google Drive API** is enabled.
- Ensure the **Android OAuth client** has the correct package name and SHA-1.
- The app will use Google Sign-In; the Android client is matched automatically.

---

## Step 5: Optional – Web Client ID (for ID token)

If you later add a backend, create a **Web application** OAuth client and add it to `local.properties`:

```
DRIVE_WEB_CLIENT_ID=YOUR_WEB_CLIENT_ID.apps.googleusercontent.com
```

Then add to `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "DRIVE_WEB_CLIENT_ID", "\"${project.findProperty("DRIVE_WEB_CLIENT_ID") ?: ""}\"")
```

For Drive-only (no backend), the Android client is sufficient.
