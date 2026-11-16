# AURA roadmap

## Batch 1 — merged foundation
- Android Kotlin/Compose scaffold, cloud-first architecture, AGPL-3.0-only licensing, security baseline and CI.

## Batch 2 — merged assistant foundation
- HTTPS OpenAI-compatible streaming chat and configurable model endpoint.
- Android Keystore-encrypted endpoint credentials and local SQLite chat history.
- Android speech-recognition handoff and text-to-speech.
- Explicit app-launch and calendar handoff actions.
- Confirmed Home Assistant service controls.
- Declarative extension manifest validation.

## Batch 3 — merged memory, voice and action safety
- User-managed memory with add, edit, delete and delete-all.
- Memory use is opt-in and bounded in prompts.
- Push-to-talk transcript review and stop-voice control.
- Package-name validation and bounded calendar reminder titles.

## Batch 4 — implementation in progress
- Encrypt memory records with Android Keystore AES-GCM and migrate existing plaintext memory.
- Export memory through an explicit user action.
- Add image input with client-side resize/compression and a 1.5 MB upload ceiling.
- Add bounded text/Markdown/CSV/JSON document input (32 KB); PDF extraction is not included yet.
- Add optional HTTPS SearXNG web search configured by the user.
- Add local scheduled reminders through WorkManager and notification permission.
- Add a built-in declarative extension catalogue with per-extension grants; no downloaded code execution.
- Add unit tests and update architecture/privacy documentation.

## Remaining production work
1. A managed identity/token broker and production hosted open-weight inference endpoint.
2. OCR and PDF/document extraction, multimodal provider compatibility checks, and image privacy controls.
3. Wake-word detection only after opt-in foreground-service, battery, privacy and device testing.
4. Typed action planner with risk tiers, confirmations, action history, cancellation and more device integrations.
5. Smart-home discovery, device picker and additional local protocols.
6. Signed third-party extension packages, publisher verification, catalogue updates and revocation.
7. Rich automation triggers/conditions, editing, cancellation and delivery reliability tests.
8. Offline speech recognition/TTS selection with explicit language-pack availability.
9. Instrumented tests on supported Android versions, accessibility, security and performance review.
10. Release signing, reproducible builds, APK size review, privacy policy and distribution.

A feature is not considered production-complete until it has automated tests, a successful Android build and documented device validation. A hosted GPU endpoint is separate infrastructure and must not be represented as free or unlimited.
