# AURA

AURA is an open-source Android personal AI assistant with a cloud-first, mobile-light design. Model weights are served remotely by an OpenAI-compatible endpoint; they are not bundled in the phone app.

## Current development status
**Development Batch 2 — usable assistant workflows in progress.** The app now includes a configurable streaming chat client, local conversation history, encrypted endpoint credentials, speech-recognition handoff, text-to-speech, explicit app launch, calendar handoff and confirmed Home Assistant service controls. See [the batch implementation notes](docs/COMPLETE_ASSISTANT_BATCH.md).

This is not yet a complete production assistant. AURA still needs a managed token broker/hosted endpoint, wake word, dedicated long-term memory manager, image/document understanding, web search, advanced action planner, extension catalogue/runtime, automation engine, broader device integrations and release-grade device/security testing.

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
