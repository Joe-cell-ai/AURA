# AURA

AURA is an open-source Android personal AI assistant with a cloud-first, mobile-light design. Model weights are served remotely by an OpenAI-compatible endpoint and are not bundled in the phone app.

## Current release line

**Version 0.6.0 — consolidated assistant reliability and security batch.**

AURA includes configurable OpenAI-compatible chat, encrypted local conversation memory, image and bounded text-document attachments, optional SearXNG search, push-to-talk speech recognition, text-to-speech, local one-time/daily/weekly reminders, explicit app and calendar actions, Home Assistant device discovery and confirmed controls, a declarative extension catalogue, and an action audit history.

The dark navy, cyan, blue and violet interface provides a quick-access dashboard for chat, memories, automations and extensions. Settings include provider, search and Home Assistant connection details. The voice-reply preference is stored in encrypted settings.

## Cloud AI

The Android app connects to a user-configured HTTPS OpenAI-compatible chat-completions endpoint. vLLM is a recommended self-hosting option; LiteLLM can optionally provide routing and quotas. GPU hosting is separate infrastructure and is not automatically free. See [docs/SELF_HOSTING.md](docs/SELF_HOSTING.md).

## Privacy and safety

- No LLM weights are bundled or downloaded by the app.
- Provider credentials and user-managed memories are encrypted using Android Keystore-backed encryption.
- Memory is opt-in; memories are not silently extracted from conversations.
- Conversation history is stored locally and can be cleared.
- Speech input is handed to the installed Android speech recognizer; AURA does not send raw microphone recordings to the model endpoint.
- Smart-home requests require an explicit confirmation and validate HTTPS origin, entity domain and supported service.
- Extensions are declarative permission grants only; AURA does not execute downloaded extension code.
- Attachments are bounded: images are resized/compressed and text documents are limited to 32 KB.

## Known limitations

PDF extraction/OCR, always-on wake word, event-driven automation rules, executable third-party extensions, offline model/voice packs and a managed hosted AI account service are not implemented. Daily and weekly reminders use WorkManager, so Android may defer delivery for battery and background limits. Real-device validation and release signing are still required before production distribution.

## Build

Requirements: JDK 17, Android SDK 35, Android Gradle Plugin 8.10.1 and Gradle 8.11.1.

```bash
gradle :app:assembleDebug
gradle :app:testDebugUnitTest
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`. GitHub Actions uploads a debug APK artifact after a successful CI run.

## License

AURA source is AGPL-3.0-only. Third-party dependencies, models and voice assets retain their own licenses. See `LICENSE`, `THIRD_PARTY_NOTICES.md` and `MODEL_LICENSES.md`.
