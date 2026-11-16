# AURA roadmap

## Development Batch 1 — merged foundation
- Android Kotlin/Compose scaffold, cloud-first architecture, licensing, security baseline and CI.

## Development Batch 2 — current consolidated implementation
- HTTPS OpenAI-compatible streaming chat and configurable model endpoint.
- Keystore-encrypted credentials and local SQLite chat history.
- Android speech-recognition handoff and text-to-speech.
- Explicit app-launch and calendar handoff actions.
- Confirmed Home Assistant service controls.
- Declarative extension manifest validation.

## Remaining development
1. Managed identity/token broker and a production hosted open-weight inference endpoint.
2. User-controlled long-term memory with inspect/edit/export/delete and retrieval.
3. Push-to-talk refinements, interruption, offline STT/TTS selection and wake-word feasibility.
4. Typed Android action planner, permission tiers, confirmations, action log and cancellation.
5. Smart-home discovery, device picker, Home Assistant entity support and more local protocols.
6. Multimodal image/document input, OCR and web search with explicit upload controls.
7. Declarative extension SDK, signed manifests, catalogue, permission grants and revocation.
8. Automation engine with schedules, triggers, conditions, retries and action history.
9. Instrumented tests on supported Android versions, privacy/security review, accessibility and performance.
10. Release signing, reproducible builds, APK size review and distribution.

No feature should be marked complete until it has automated tests, a successful Android build and documented device validation. A hosted GPU endpoint is a separate infrastructure decision and must not be represented as free or unlimited.
