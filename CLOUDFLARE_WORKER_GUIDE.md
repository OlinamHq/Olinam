# Oj AI - Cloudflare Worker Backend Guide

## 1. Cloudflare Worker URL
The app is configured to use Cloudflare Worker for Oj AI.
- **Default Dummy URL**: `https://oj-ai-worker.olinam.workers.dev/api/chat`
- **Location in Code**: `app/src/main/java/com/example/ai/OjAiService.kt`
```kotlin
var CLOUDFLARE_WORKER_URL: String = "https://oj-ai-worker.olinam.workers.dev/api/chat"
```

## 2. In-App URL Configuration (No Code Changes Needed!)
You can also change the Worker URL directly from within the app:
1. Open **Oj AI** from the chat list.
2. Tap the 3 dots in the top right -> **Profile & Worker Settings**.
3. Under **AI Personality & Tuning**, tap **Cloudflare Worker Endpoint**.
4. Enter your new Worker URL and tap **Save URL**.

## 3. Ready-to-Deploy Cloudflare Worker Script
We have provided a complete production-ready worker script located at:
`workers/oj-ai-worker.js`

### What it does:
- Accepts `POST` requests with JSON:
  ```json
  {
    "prompt": "Hello Oj AI",
    "message": "Hello Oj AI",
    "history": [
      { "role": "user", "content": "Hi" },
      { "role": "assistant", "content": "Hello! How can I help?" }
    ],
    "model": "oj-ai"
  }
  ```
- Returns clean JSON:
  ```json
  {
    "response": "...",
    "status": "success",
    "provider": "cloudflare-worker"
  }
  ```
- Supports Cloudflare Workers AI (`@cf/meta/llama-3.1-8b-instruct`), Google Gemini, and fallback responses.
- Full CORS headers enabled for mobile app requests.

### How to deploy to Cloudflare:
```bash
npx wrangler deploy workers/oj-ai-worker.js --name oj-ai-worker
```
