# Offline AI (Gemini Nano)

## Status: Placeholder (build-blocked)

ML Kit genai-prompt requires Kotlin 2.2+, which triggers KSP "unexpected jvm signature V" with Hilt. Dependency removed; OfflineGeminiProvider is stubbed. Re-enable when Dagger/KSP fix is released.

## Requirements

- Android AICore SDK (device-dependent; Pixel 8+, selected Samsung)
- `com.google.android.aicare` or AICore system app
- Model download on first use

## Implementation

- **OfflineGeminiProvider** (`com.studyasist.ai`): ML Kit `Generation.getClient()`, `checkStatus()`, `download()`, `generateContent(prompt)`
- **GeminiRepository.generateContentWithFallback(apiKey, prompt)**: Tries cloud first; on failure, uses `OfflineGeminiProvider` if `isAvailable()`
- **ExplainViewModel** & **SolveViewModel**: Use `generateContentWithFallback` for explain/solve flows

## Use Cases

- Basic Explain/Solve when offline
- Fallback when user has no API key or cloud API errors
