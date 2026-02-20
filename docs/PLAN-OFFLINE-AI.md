# Offline AI (Gemini Nano)

## Status: Placeholder

On-device Gemini Nano for basic AI operations without internet is planned for future implementation.

## Requirements

- Android AICore SDK (device-dependent; Pixel 8+, selected Samsung)
- `com.google.android.aicare` or AICore system app
- Model download on first use

## Use Cases

- Basic Explain/Solve when offline
- Fallback when user has no API key

## Implementation Notes

- Check `AICoreClient.isAvailable()` before offering offline AI
- Graceful fallback to "Requires network" or "Add API key" when unavailable
