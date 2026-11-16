# AURA roadmap and delivery status

## Merged foundation and assistant batches
- Android Kotlin/Compose application with AGPL-3.0-only licensing and Android CI.
- Cloud-first OpenAI-compatible chat; model weights are not bundled in the APK.
- Android Keystore-encrypted provider credentials and AES-GCM encrypted, opt-in user-managed memories.
- Local conversation history, image input, bounded text/Markdown/CSV/JSON attachments, and configurable SearXNG search.
- Push-to-talk speech recognition with transcript review, text-to-speech, voice-reply toggle and stop control.
- Explicit app-launch and calendar handoff actions, typed action validation and bounded audit history.
- Home Assistant discovery for supported entities with explicit confirmation before device actions.
- Declarative extension catalogue with per-extension grants; third-party executable code is not downloaded or run.
- One-time, daily and weekly local reminders through WorkManager.

## Final consolidated assistant batch — 0.6.0
- Harden Home Assistant endpoint validation: HTTPS origin only, no embedded credentials, path, query or fragment.
- Validate entity IDs and ensure requested service domain matches the selected entity.
- Restrict available service buttons by device domain (light/switch/fan, climate, scene/script).
- Reject blank or overlong one-time reminder titles instead of silently replacing them.
- Persist the voice-reply preference in encrypted settings.
- Add focused unit tests for Home Assistant URL, entity and service validation.
- Update app version and delivery documentation.

## Explicitly not claimed as complete
- **PDF/OCR:** text documents and images are supported; PDF text extraction and OCR are not in this build.
- **Wake word:** no always-listening background microphone service. Voice remains explicit push-to-talk through Android's recognizer.
- **Advanced event automation:** one-time/daily/weekly reminders work; event-driven triggers, complex conditions, rule editing and an automation history screen remain future work.
- **Extension marketplace:** the catalogue is declarative and permission-gated. It does not install or execute downloaded third-party code; signed package distribution and revocation remain future work.
- **Hosted AI service:** AURA connects to a provider configured by the user. Production hosting, account identity, quotas and shared token brokerage require separately operated infrastructure.
- **Offline AI and speech packs:** not bundled. Speech recognition availability depends on Android recognizer and installed language packs.
- **Release/device validation:** CI builds and unit tests are necessary but do not replace testing on physical Android devices, accessibility review, release signing or security assessment.

A feature is production-complete only after automated tests, a successful Android build and documented device validation. Android WorkManager is battery-aware and may defer reminder delivery; it is not an exact alarm clock.
