# AURA Cloud AI Architecture (cloud-first, mobile-light)

## Goal
Keep the Android APK and installed footprint small by running open-weight language models on a remote inference server. The phone is a thin client: it captures user input, sends a compact request over HTTPS, renders the response, and stores only the user's chosen conversation history locally.

## Recommended open-source serving stack
- **vLLM**: model inference server for supported open-weight models; exposes an OpenAI-compatible API. Deploy it on a GPU host or a self-managed server.
- **LiteLLM (optional gateway)**: a provider-neutral API gateway for routing, quotas, model aliases, and usage accounting. Keep it replaceable; AURA's client must not depend on a proprietary vendor API.
- **PostgreSQL (optional server-side metadata)**: store account/configuration metadata only when required. Do not persist raw prompts or completions by default.
- **Android Room**: local conversation history and preferences. Keep this local by default; optional sync must be explicit and end-to-end protected.

These are software components, not free GPU hosting. AURA can be configured to use a community/free endpoint where available, but free tiers have limits and can change. A production hosted model needs compute somewhere; support self-hosting and multiple compatible endpoints to avoid lock-in.

## Request flow
1. User submits text or speech; speech is converted to text on-device where feasible.
2. AURA applies a context budget and removes unnecessary metadata.
3. The app sends only the current message and a compact, relevant context window over TLS.
4. The gateway routes to the configured open-weight model server.
5. The response is streamed back and rendered; the client does not download model weights.
6. Conversation history is saved locally only according to the user's retention setting.

## Mobile data and storage controls
- Do not bundle LLM weights, speech models, or large datasets in the APK.
- Default to text-only requests; images/audio are sent only after clear user action and with size limits.
- Use streaming responses, bounded output tokens, compact JSON, and context-window trimming.
- Avoid retransmitting full conversation history; send a short rolling context or server-issued conversation handle where supported.
- Cache static configuration and model metadata; do not cache private prompts in shared caches.
- Provide a per-session data estimate and a low-data mode that disables automatic image/audio uploads and shortens responses.
- Make local history retention configurable; default server-side prompt logging and training use to off in AURA-controlled deployments.

## Privacy and security requirements
- HTTPS/TLS only; never ship a shared server API key inside the APK.
- Use short-lived user tokens or a backend-issued credential. Store any device credential using Android Keystore-backed encryption.
- Treat cloud providers as able to process submitted prompts. Clearly show the active endpoint and disclose its retention/training policy.
- Provide a cloud-off mode and a clear conversation-delete control.
- Sensitive Android actions must be validated and confirmed on-device; model output is untrusted input, never an authorization decision.
- Redact secrets and sensitive notification content before any cloud request unless the user explicitly chooses to share it.

## Model policy
Use open-weight models only after checking the exact model's licence, usage restrictions, attribution, and redistribution terms. Model weights are not automatically covered by AURA's AGPL licence. Keep model name, revision, source, and licence in `MODEL_LICENSES.md`.

## Deployment profiles
- **Development**: developer-run vLLM endpoint; no user data retention.
- **Community demo**: rate-limited endpoint, explicit warning that availability and free quotas are not guaranteed.
- **Self-hosted**: users or organisations deploy the same API contract on their own infrastructure.

The Android app must remain endpoint-agnostic and must not assume one cloud host is permanently free or available.
