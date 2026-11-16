# AURA

AURA is an open-source Android personal AI assistant with a cloud-first, mobile-light design. Model weights are served remotely by an OpenAI-compatible endpoint; they are not bundled in the phone app.

## Current development status
**Development Batch 4 — multimodal input, web search, scheduled reminders and encrypted memory in validation.** The app includes configurable streaming chat, local conversation history, encrypted endpoint credentials, push-to-talk speech recognition, text-to-speech, explicit app launch, calendar handoff, confirmed Home Assistant controls and opt-in user-managed memory (add/edit/delete/export/clear) encrypted with Android Keystore. Memory is not automatically extracted from chats and is only sent with AI prompts when enabled. Batch 4 adds image input, bounded text-document input, optional SearXNG search, local scheduled reminders and a declarative extension catalogue. See [the batch implementation notes](docs/COMPLETE_ASSISTANT_BATCH.md).

This is not yet a complete production assistant. AURA still needs a managed token broker/hosted endpoint, wake word, PDF/OCR support, a full typed action planner, smart-home discovery, signed third-party extensions, advanced automation triggers and release-grade device/security testing. The current search integration requires a user-configured HTTPS SearXNG endpoint; no shared search service is bundled.

## Cloud AI
The Android app talks to a user-configured HTTPS OpenAI-compatible `/v1/chat/completions` endpoint. vLLM is the recommended self-hostable inference server; LiteLLM can optionally provide routing and quotas. These projects are open source, but GPU hosting is not automatically free. Community free tiers may be limited or unavailable.

## Privacy and data usage
- No LLM weights are downloaded or bundled.
- Credentials entered in Settings are encrypted with Android Keystore AES-GCM.
- Conversation history is stored locally and can be cleared.
- Recent context and response tokens are bounded.
- Requests require HTTPS; there is no shared API key in the APK.
- Speech input is delegated to the installed Android recognizer; AURA does not upload raw microphone recordings to the model endpoint.
- Smart-home actions require a user confirmation.

## Build
Requirements: JDK 17, Android SDK 35, Android Gradle Plugin 8.10.1 and Gradle 8.11.1.

```bash
gradle :app:assembleDebug
gradle :app:testDebugUnitTest
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`. GitHub Actions uploads the debug APK as an artifact after a successful CI run.

## License
AURA source is AGPL-3.0-only. Third-party dependencies, models and voice assets retain their own licenses. See `LICENSE`, `THIRD_PARTY_NOTICES.md` and `MODEL_LICENSES.md`.
