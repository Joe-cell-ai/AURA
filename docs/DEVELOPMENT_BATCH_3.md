# Development Batch 3 — user-managed memory

## Included in this branch
- Local SQLite memory store separate from conversation history.
- User can add a fact, edit it, delete one fact, or delete all saved memories.
- Memory is opt-in; AURA does not silently extract or persist personal facts.
- At most 100 saved facts are retained; each fact is limited to 500 characters.
- At most 10 facts and 1,600 characters are included in a cloud chat prompt.
- Prompt framing explicitly says saved facts are reference context, not instructions.
- Unit tests cover empty memory, size bounds and instruction isolation.

## Privacy note
Saved memories are stored locally in the app's SQLite database. They are not encrypted at rest in this batch. When cloud chat is used, the selected bounded memory text is sent to the configured AI endpoint as part of the prompt. The Memory screen explains this and provides edit/delete controls. Users should avoid storing secrets, passwords, financial account numbers or other highly sensitive data.

## Validation
The Android CI run for the previous merged batch failed during Android SDK setup before Gradle build/tests could run. This branch must establish a green build and test run before the memory feature is considered complete or a test APK is shared.