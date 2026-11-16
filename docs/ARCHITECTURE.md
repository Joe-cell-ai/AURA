# Architecture

## Android client
Kotlin + Jetpack Compose. The client owns UI, local preferences, optional local conversation history, permission prompts, and final authorization for device actions. Room is the planned local persistence layer. The app must not bundle model weights.

## AI gateway boundary
A provider-neutral `AiProvider` contract isolates the Android app from vendors. The cloud-first deployment targets an OpenAI-compatible API backed by open-weight models served with vLLM. An optional LiteLLM gateway can route among compatible model servers and apply quotas. Endpoints are configurable; no provider key is embedded in the APK.

## Data path
User input -> local privacy/context filter -> compact HTTPS request -> configured inference endpoint -> streamed response -> UI. Context is bounded; only relevant recent turns are sent. Raw prompt logging is disabled by default in AURA-controlled server deployments. The user can disable cloud AI.

## Optional capabilities
- On-device speech recognition where practical to avoid uploading raw audio.
- Optional separately downloaded local models for future offline mode; never required for basic app installation.
- Memory, actions, smart-home, automation, and extension modules remain separate from the model provider.

## Trust boundaries
LLM output is untrusted. It cannot grant permissions or execute Android actions directly. A typed action layer validates arguments and obtains user confirmation for sensitive operations. Cloud requests disclose that prompt content is processed by the selected endpoint.
