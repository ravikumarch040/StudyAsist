# StudyAsist – Plan & Design: Study Tools (Voice, Dictate, Explain, Solve)

**Version:** 1.0  
**Scope:** Change Voice (Settings), Dictate, Explain, Solve

---

## 1. Overview

| Feature       | Purpose | Input | Output | Network |
|---------------|---------|--------|--------|---------|
| **Change Voice** | Set TTS voice for alarm/speech | Settings UI | Applied to all TTS (alarm, Dictate, Explain, Solve) | No |
| **Dictate**   | Read aloud text from a photo   | Camera / upload image | OCR → text → TTS in chosen language | Optional (OCR) |
| **Explain**   | Explain text or image content | Text and/or image (camera/file) | AI explanation in language | Yes (AI API) |
| **Solve**     | Step-by-step solution to a problem | Text and/or image (camera/file) | AI step-by-step solution | Yes (AI API) |

---

## 2. Change Voice (Settings)

### 2.1 Goal
Let the user pick the **speech voice** (and optionally style) used for:
- Alarm TTS message
- Dictate (read-aloud)
- Explain / Solve (read-aloud of AI response, if offered)

### 2.2 Technical Approach

- **Android TextToSpeech** already supports multiple voices: `TextToSpeech.getVoices()` returns `Set<Voice>` (name, locale, quality, latency).
- **Settings:** New section “Speech / Voice” with a single selector: **Voice** (dropdown or list of available voices for the default locale + optionally “System default”).
- **Persistence:** DataStore key e.g. `tts_voice_name` (String?) — store `Voice.name` or null for default. Optional: `tts_locale` (String?) for e.g. `"en-US"` if we want to filter voices by locale.
- **Apply everywhere:** When creating or using `TextToSpeech`, call `setVoice(voice)` before speaking (in `ReminderAlarmActivity`, `AlarmTtsService`, and any new Dictate/Explain/Solve TTS). If saved voice is null or unavailable, fall back to default (e.g. `setLanguage(Locale.getDefault())` and do not set voice).

### 2.3 Settings UI (wireframe)

```
┌─────────────────────────────────────────┐
│  ←  Settings                            │
├─────────────────────────────────────────┤
│  ... (existing: Notifications, Sound,  │
│       Vibration, Alarm TTS message)     │
│  ─────────────────────────────────────  │
│  Speech                                 │
│  Voice for alarms & reading             │
│  [System default                    ▾]  │  ← or list: English (US) - Female, etc.
└─────────────────────────────────────────┘
```

### 2.4 Data / Code Touchpoints

- **SettingsDataStore:** Add `ttsVoiceName: stringPreferencesKey("tts_voice_name")` (nullable).
- **SettingsRepository:** `getTtsVoiceName(): Flow<String?>`, `setTtsVoiceName(String?)`. Optionally expose `availableVoices(): List<Voice>` (from TTS engine) for the picker.
- **NotificationScheduler / ReminderAlarmActivity / AlarmTtsService:** When starting TTS, resolve saved voice name to `Voice` and call `tts.setVoice(voice)` if found.
- **New:** Use the same resolved voice in Dictate, Explain, and Solve when they use TTS.

---

## 3. Dictate

### 3.1 Goal
User captures or uploads an **image** of a paragraph/chapter → app **extracts text (OCR)** → **reads it aloud** in the chosen language (TTS).

### 3.2 Flow

1. User opens **Dictate** screen.
2. **Input:** “Take photo” (camera) or “Upload image” (gallery / file picker).
3. User selects or captures an image.
4. User taps **“Dictate”** (or “Read aloud”).
5. App runs **OCR** on the image → plain text.
6. Optional: **Language** selector (e.g. “Language for reading”) so TTS uses that locale.
7. App uses **TTS** to read the text (respecting **Change Voice** setting). Optional: show extracted text in a scrollable area and highlight sentence/word while reading (stretch goal).

### 3.3 Technical Choices

| Component | Recommendation | Alternative |
|-----------|----------------|-------------|
| **OCR** | **ML Kit Text Recognition** (Google, on-device) | Google Cloud Vision API (network); Tesseract (heavier, on-device) |
| **Image source** | Camera (CameraX or Activity result) + Gallery (ActivityResultContracts.GetContent for image/*) | Same |
| **TTS** | Android `TextToSpeech` (existing) with selected voice and language | — |
| **Language** | User choice in Dictate screen (e.g. dropdown: English, Hindi, etc.) stored per-screen or in DataStore for “Dictate language” | Auto-detect from OCR (complex, can add later) |

### 3.4 Permissions

- **Camera:** `android.permission.CAMERA` (runtime for API 23+).
- **Read storage / media:** For gallery pick, use `READ_MEDIA_IMAGES` (API 33+) or `READ_EXTERNAL_STORAGE` (legacy); `ActivityResultContracts.GetContent()` often avoids storage permission on modern Android.

### 3.5 Dictate UI (wireframe)

```
┌─────────────────────────────────────────┐
│  ←  Dictate                              │
├─────────────────────────────────────────┤
│  Add image of paragraph or chapter      │
│                                         │
│  ┌─────────────┐  ┌─────────────┐      │
│  │  📷         │  │  📁         │      │
│  │  Take photo │  │  Upload     │      │
│  └─────────────┘  └─────────────┘      │
│                                         │
│  [  Preview of selected image  ]        │
│                                         │
│  Language for reading    [English   ▾]  │
│                                         │
│  [        Dictate (Read aloud)        ] │
│                                         │
│  ─── Extracted text (optional) ───      │
│  ┌─────────────────────────────────┐   │
│  │ The quick brown fox...           │   │
│  └─────────────────────────────────┘   │
└─────────────────────────────────────────┘
```

### 3.6 Architecture

- **Screen:** `DictateScreen` (Compose).
- **ViewModel:** `DictateViewModel` — holds image URI/bitmap, runs OCR in a coroutine (e.g. `Dispatchers.Default`), exposes extracted text + state (loading, error), triggers TTS (or exposes “text to speak” for a TTS helper).
- **Use case / repository:** Optional `OcrRepository` or `TextRecognitionHelper` wrapping ML Kit; called from ViewModel. TTS can be a shared `TtsHelper` that takes text + locale + voice and speaks (used by Dictate, Explain, Solve).

---

## 4. Explain

### 4.1 Goal
User provides **text** and/or **image** (camera or upload) → app sends content to an **AI** service with a prompt like “Explain this in simple terms in [language]” → show **explanation** and optionally read it aloud.

### 4.2 Flow

1. User opens **Explain** screen.
2. **Input:**  
   - Text: multiline field “Paste or type text to explain”.  
   - Image: “Take photo” / “Upload image” (if image: run OCR first, then send resulting text to AI; or send image if API supports vision).
3. User optionally selects **language** for the explanation (e.g. “Explain in: [English ▾]”).
4. User taps **“Explain”**.
5. App sends request to **AI API** (see below) with combined text (and optionally image).
6. Show **explanation** in a scrollable area; optional **“Read aloud”** button using TTS (with Change Voice).

### 4.3 Technical Choices

| Component | Recommendation | Notes |
|-----------|----------------|-------|
| **AI API** | **Google Gemini** (Gemini API, supports text + image, good for mobile) or **OpenAI** (GPT-4o for vision) | Requires API key; store in BuildConfig or secure storage; network required |
| **Input** | Text field + image (camera/gallery). If image: OCR (ML Kit) → text, then send text to AI; or use Vision API (Gemini/GPT-4o) with image URL/base64 | Vision reduces OCR dependency; OCR keeps one path for “text only” from image |
| **Language** | User selector “Explain in: [locale]” in the prompt | Simple and explicit |

### 4.4 Explain UI (wireframe)

```
┌─────────────────────────────────────────┐
│  ←  Explain                              │
├─────────────────────────────────────────┤
│  Add text or image to explain           │
│                                         │
│  ┌─────────────────────────────────────┐│
│  │ Type or paste text here...           ││
│  │                                     ││
│  └─────────────────────────────────────┘│
│                                         │
│  ┌─────────────┐  ┌─────────────┐      │
│  │  📷 Photo   │  │  📁 Upload   │      │
│  └─────────────┘  └─────────────┘      │
│                                         │
│  Explain in         [English        ▾]  │
│                                         │
│  [            Explain                  ]│
│                                         │
│  ─── Explanation ───                   │
│  ┌─────────────────────────────────────┐│
│  │ This paragraph describes...         ││
│  └─────────────────────────────────────┘│
│  [       Read aloud       ]             │
└─────────────────────────────────────────┘
```

### 4.5 Architecture

- **Screen:** `ExplainScreen`.
- **ViewModel:** `ExplainViewModel` — input text, image URI, language; calls a **repository** or **use case** that talks to the AI API (Gemini/OpenAI); exposes explanation text + loading/error; optional “read aloud” triggers TTS.
- **Backend:** `ExplainRepository` or `AiRepository` — one method e.g. `suspend fun explain(text: String, imageUriOrBytes: Any?, language: String): Result<String>`. Use Retrofit/OkHttp or Gemini SDK for Android. API key from BuildConfig or user input in Settings (advanced).

---

## 5. Solve

### 5.1 Goal
User provides a **problem** as text and/or image → app sends it to an **AI** with a prompt like “Solve this step by step in [language]” → show **step-by-step solution**; optional read aloud.

### 5.2 Flow
Same as Explain, but:
- Prompt: “Solve this problem step by step. Explain each step clearly. Output in [language].”
- Output: Step-by-step solution (markdown or plain text); optional TTS.

### 5.3 Technical Choices
- **Same AI API** as Explain (Gemini or OpenAI).
- **Same input** (text + optional image via OCR or vision).
- **Repository:** `SolveRepository` or extend `AiRepository` with `solve(problemText: String, image: Any?, language: String): Result<String>`.

### 5.4 Solve UI (wireframe)

```
┌─────────────────────────────────────────┐
│  ←  Solve                                │
├─────────────────────────────────────────┤
│  Add problem (text or image)            │
│                                         │
│  ┌─────────────────────────────────────┐│
│  │ Enter or paste the problem...        ││
│  └─────────────────────────────────────┘│
│                                         │
│  ┌─────────────┐  ┌─────────────┐      │
│  │  📷 Photo   │  │  📁 Upload   │      │
│  └─────────────┘  └─────────────┘      │
│                                         │
│  Solution language  [English        ▾]  │
│                                         │
│  [            Solve                     ]│
│                                         │
│  ─── Step-by-step solution ───          │
│  ┌─────────────────────────────────────┐│
│  │ Step 1: ...                          ││
│  │ Step 2: ...                          ││
│  └─────────────────────────────────────┘│
│  [       Read aloud       ]             │
└─────────────────────────────────────────┘
```

---

## 6. Navigation & Entry Points

- **Home** (or a new “Study” tab): Add entry points to **Dictate**, **Explain**, **Solve** (e.g. cards or a bottom nav / drawer item “Study tools” that opens a sub-menu or a single “Tools” screen with three buttons).
- **NavRoutes:** Add `DICTATE`, `EXPLAIN`, `SOLVE` (e.g. `"dictate"`, `"explain"`, `"solve"`).
- **Settings:** Add “Speech / Voice” section (Change Voice); optionally “API key” or “AI provider” for Explain/Solve (if key is user-provided).

Suggested navigation:

```
Home (Today | Timetables)
   ├── Settings (existing + Change Voice)
   └── Study tools (new)
         ├── Dictate
         ├── Explain
         └── Solve
```

---

## 7. Dependencies (Gradle)

| Feature   | Dependency | Purpose |
|-----------|------------|---------|
| Dictate   | ML Kit Text Recognition | OCR on-device |
| Explain   | Retrofit + OkHttp, or Gemini Android SDK | Call Gemini/OpenAI API |
| Solve     | Same as Explain | Same API, different prompt |
| Change Voice | None (built-in TTS) | Use existing TextToSpeech APIs |

- **ML Kit:** `com.google.mlkit:text-recognition` (and optionally language-specific modules, e.g. Latin, Devanagari).
- **Network:** For Gemini: `com.google.ai.client.generativeai:generativeai` or REST; for OpenAI: `com.squareup.retrofit2:retrofit` + JSON converter. API key must not be committed; use `BuildConfig` or Settings.

---

## 8. DataStore / Settings Additions

| Key | Type | Use |
|-----|------|-----|
| `tts_voice_name` | String? | Change Voice: selected TTS voice name |
| `dictate_language` | String? | Default “Dictate” reading language (e.g. "en") |
| `explain_language` | String? | Default “Explain in” language |
| `solve_language` | String? | Default “Solution language” |
| `ai_api_key` | String? | Optional: user-entered API key for Explain/Solve |
| `ai_provider` | String? | Optional: "gemini" | "openai" |

---

## 9. Implementation Order

1. **Change Voice** — Settings + DataStore + apply in existing TTS (alarm, service). No new screens beyond Settings.
2. **Dictate** — Screen, camera/gallery, ML Kit OCR, TTS; use Change Voice when available.
3. **Explain** — Screen, text + image input, AI API integration, explanation UI, optional TTS.
4. **Solve** — Screen, same input as Explain, AI with “step by step” prompt, solution UI, optional TTS.

---

## 10. Risks & Mitigations

| Risk | Mitigation |
|------|------------|
| API key exposure | Store in BuildConfig (build-time) or secure pref; never log or send to third parties except the chosen AI provider |
| No network for Explain/Solve | Show clear “No connection” / “API error”; optional offline message |
| OCR quality on poor photos | Show extracted text so user can edit before Dictate; optional “Retake” |
| TTS voice not available after OS update | Fallback to default voice if saved voice name not in `getVoices()` |
| Large images for AI | Resize/compress before upload; or send only OCR text for Explain/Solve to reduce payload |

---

## 11. Optional Enhancements (Later)

- **Dictate:** Highlight current sentence/word during TTS (using `UtteranceProgressListener` and word boundaries).
- **Explain/Solve:** Support markdown in AI response (e.g. `BasicMarkdown` or simple formatting in Compose).
- **Offline:** Prefer on-device OCR (ML Kit) for Dictate; Explain/Solve remain online unless an on-device model is integrated later.
- **History:** Save last Dictate/Explain/Solve inputs or results in Room (optional table) for “Recent” or “Favorites”.

This plan keeps the app consistent with existing MVVM, Compose, DataStore, and Hilt, and reuses TTS and (for Explain/Solve) a single AI client with two prompt types.
