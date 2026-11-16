# Self-hosting AURA inference

AURA's Android app remains a thin client. This optional Docker Compose stack runs an open-weight model with vLLM and routes requests through LiteLLM.

## Requirements
- Linux host with a supported NVIDIA GPU and current NVIDIA Container Toolkit.
- Docker Engine and Docker Compose plugin.
- Enough GPU memory for the selected model and context length. Qwen2.5-7B-Instruct generally needs a substantial GPU; quantization or a smaller model may be necessary.
- A domain name and HTTPS reverse proxy before exposing the gateway outside localhost.

## Start
1. Copy `server/.env.example` to `server/.env`.
2. Replace both API keys with independent, long random secrets. Never commit the populated file.
3. From `server/`, run `docker compose --env-file .env up -d`.
4. Check logs with `docker compose --env-file .env logs -f vllm litellm`.
5. Configure AURA with the HTTPS URL of your reverse proxy followed by `/v1/chat/completions`, model ID `aura-default`, and the LiteLLM master key.

The containers bind to localhost only. Do not change the port bindings to public interfaces unless you have configured TLS, firewall restrictions, rate limits and authentication at the edge. Never expose vLLM directly to the public internet.

## Notes
- This is a self-hosting template, not a managed service. GPU hosting, electricity, storage and bandwidth can cost money.
- The Compose file uses floating container tags for convenience. Pin tested image digests before production use.
- Model weights are downloaded by the server at runtime and are never packaged into the Android APK.
- Verify the model's current license and acceptable-use terms before deploying it.
- This template does not provision user accounts, per-user quotas, a token broker, abuse monitoring or backups. Those are required for a public multi-user service.
