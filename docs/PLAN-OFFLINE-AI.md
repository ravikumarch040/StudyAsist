# Offline AI (Gemini Nano)

## Status: Placeholder (build-blocked)

ML Kit genai-prompt requires Kotlin 2.2+, which triggers KSP "unexpected jvm signature V" with Hilt. Dependency removed; OfflineGeminiProvider is stubbed. Re-enable when Dagger/KSP fix is released.

## Implementation Checklist (when unblocked)

1. [ ] Upgrade Kotlin to 2.2+ and KSP to compatible version
2. [ ] Verify Hilt/Dagger compatibility (no "unexpected jvm signature V")
3. [ ] Add `com.google.mlkit:genai` (genai-prompt) dependency
4. [ ] Implement `OfflineGeminiProvider`:
   - `Generation.getClient(context)`, `checkStatus()`, `download()`, `generateContent(prompt)`
   - `isAvailable()` returns true when model ready
5. [ ] Enable offline path in `GeminiRepository.generateContentWithFallback()`
6. [ ] Test on Pixel 8+ or supported Samsung device

## Current Implementation

- **OfflineGeminiProvider** (`com.studyasist.ai`): Stubbed; `isAvailable() = false`
- **GeminiRepository.generateContentWithFallback**: Cloud only
- **ExplainViewModel** & **SolveViewModel**: Use fallback; offline path dormant

## Requirements

- Android AICore SDK (device-dependent; Pixel 8+, selected Samsung)
- `com.google.android.aicare` or AICore system app
- Model download on first use

## Use Cases

- Basic Explain/Solve when offline
- Fallback when user has no API key or cloud API errors
