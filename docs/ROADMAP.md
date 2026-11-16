# AURA roadmap

## Development Batch 1 — merged foundation
- Android Kotlin/Compose scaffold, cloud-first architecture, licensing, security baseline and CI.

## Development Batch 2 — merged assistant baseline
- HTTPS OpenAI-compatible streaming chat and configurable model endpoint.
- Keystore-encrypted credentials and local SQLite chat history.
- Android speech-recognition handoff and text-to-speech.
- Explicit app-launch and calendar handoff actions.
- Confirmed Home Assistant service controls.
- Declarative extension manifest validation.

## Development Batch 3 — in development
- Dedicated user-managed local memory: add, edit, delete individual facts, and delete all.
- Only user-approved facts are used in chat; no automatic personal-fact extraction.
- Memory prompt is bounded and explicitly treated as untrusted context, not instructions.
- Memory unit tests for empty, bounded and injection-resistant prompt framing.

## Remaining development
1. Managed identity/token broker and production hosted open-weight inference endpoint.
2. Voice: interruption, configurable recognizer/TTS, push-to-talk refinements and wake-word feasibility.
3. Typed Android action planner with risk tiers, confirmations, audit history and cancellation.
4. Smart-home discovery, device picker, broader Home Assistant entities and local protocols.
5. Multimodal image/document input, OCR and web search with explicit upload controls.
6. Declarative extension SDK, signed manifests, catalogue, permission grants and revocation.
7. Automation engine with schedules, triggers, conditions, retries and action history.
8. Instrumented tests on supported Android versions, privacy/security review, accessibility and performance.
9. Release signing, reproducible builds, APK size review and distribution.

No feature should be marked complete until it has automated tests, a successful Android build and documented device validation. A hosted GPU endpoint is a separate infrastructure decision and must not be represented as free or unlimited.