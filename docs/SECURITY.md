# Security baseline

- Store provider credentials using Android Keystore-backed encryption.
- Do not log prompts, transcripts, secrets or document contents by default.
- Keep tool execution separate from model inference.
- Require explicit confirmation for messaging, calls, purchases, deletion, account changes and other consequential actions.
- Use least-privilege Android permissions.
- Show a persistent/visible listening state while recording.
- Extensions must declare permissions and use a restricted API.
- Provide memory inspection, export and deletion controls.
- Record action outcomes without retaining unnecessary sensitive payloads.
