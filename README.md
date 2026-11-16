# AURA

AURA is a privacy-first, lightweight, open-source AI assistant for Android. It uses a **cloud-first open-weight LLM architecture** so users do not need multi-gigabyte model files on their phones. The Android app is a thin client for configurable OpenAI-compatible inference endpoints; vLLM is the recommended self-hostable serving engine.

## Product priorities
- Keep APK and installed footprint small; never bundle LLM weights.
- Reduce mobile data with compact prompts, bounded context/output, streaming and low-data mode.
- Support open-weight models and replaceable endpoints rather than vendor lock-in.
- Keep conversation history local by default; disable server prompt logging by default in AURA-controlled deployments.
- Make cloud processing visible and allow users to disable cloud AI.

## Current status
**Development batch 1 — foundation scaffold.** The starter UI and provider abstractions are scaffolding, not a functioning AI or voice assistant. A production cloud endpoint, microphone capture, speech recognition, wake word, memory database and Android action execution are not wired yet.

## Cloud AI design
See [Cloud AI architecture](docs/CLOUD_AI_ARCHITECTURE.md). Proposed stack: vLLM for open-weight model serving and optional LiteLLM for routing and quotas. These components are open source, but GPU hosting is not automatically free. Free community tiers may be limited or unavailable. AURA will support self-hosting and multiple compatible endpoints.

## Build
Requirements: JDK 17, Android SDK 35, Android Gradle Plugin 8.10.1 and Gradle 8.11.1.

```bash
gradle :app:assembleDebug
gradle :app:testDebugUnitTest
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## Privacy and mobile data
- Default to text-only requests; send images/audio only after user action.
- Trim context and cap output tokens to limit data transfer.
- Never embed provider API keys in the APK.
- Use HTTPS and short-lived credentials from a trusted backend.
- Show the active endpoint and its retention policy.
- Cloud-off mode must remain available.

## License
AURA source is intended for AGPL-3.0-only. Third-party dependencies, model weights and voice assets retain their own licenses. See `LICENSE`, `THIRD_PARTY_NOTICES.md` and `MODEL_LICENSES.md`.
