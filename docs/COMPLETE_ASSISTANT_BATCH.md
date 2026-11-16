# Complete assistant batch — development scope

This batch builds on the merged foundation and adds the first usable AURA workflows.

## Included
- OpenAI-compatible HTTPS chat-completions client with server-sent-event streaming.
- Configurable HTTPS endpoint, model ID and optional user-owned API key.
- Android Keystore AES-GCM encryption for AI credentials and Home Assistant token.
- Local SQLite conversation history, bounded recent context and clear-history action.
- Speech-to-text handoff through Android's recognizer intent, requesting offline recognition preference.
- Text-to-speech replies with an in-app toggle.
- User-invoked app launch by package name.
- Calendar reminder handoff, with a confirmation dialog before opening the calendar.
- Home Assistant service controls for configured entity IDs, with confirmation before each action.
- Declarative extension manifest validation. Arbitrary downloaded executable code is not supported.
- Unit tests for extension capability validation.
- CI workflow builds the debug APK and runs unit tests.

## Configure and test
1. Open AURA Settings.
2. Enter a complete HTTPS OpenAI-compatible chat-completions URL, model ID, and optional personal API key.
3. Save settings and send a short test prompt.
4. Optional: configure a Home Assistant HTTPS URL, bearer token and entity ID (for example, `light.living_room`). Use the explicit Turn on/off/toggle controls; AURA asks before sending a service call.
5. Try the microphone button. It launches the installed Android speech recognizer and asks it to prefer offline recognition; actual offline support depends on the recognizer installed on the device.
6. Use Clear chat to delete locally stored conversation history.

## Privacy and safety
- No LLM weights are bundled in the APK.
- No API credentials are hard-coded. User-entered secrets are encrypted with an Android Keystore AES-GCM key.
- Chat history is stored locally in SQLite; the user can clear it.
- Requests use HTTPS only, recent context is limited to 12 messages and each message is bounded.
- The app does not send microphone recordings directly to the LLM endpoint.
- Model text is never executed as an Android command.
- App launch is initiated by a user tap; calendar and smart-home actions have confirmation UI.
- Home Assistant domains and service names are allow-listed.

## Remaining roadmap (not claimed as complete)
- AURA-managed account/token broker and a production hosted inference endpoint.
- Wake-word detection and background listening (requires careful battery/privacy design).
- Dedicated memory manager with user-approved long-term facts, edit/export/delete.
- Rich multimodal image/document input and web search.
- Full action planner with typed schemas, risk tiers, audit history and cancellation.
- Home Assistant discovery/device picker, broader smart-home integrations and device testing.
- Extension SDK, signed declarative package format, catalogue, permissions and revocation UX.
- Automation scheduler, triggers, conditions and reliable background execution.
- Instrumented/device tests, security review, accessibility, performance and release signing.

The code is a development build, not a production-secure hosted service. Public deployments must use a trusted backend to issue short-lived credentials; never embed a shared server secret in the APK.
