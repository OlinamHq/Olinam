# Step 2: Cloudflare R2 media

Client can now upload image, voice, video, and document, then send the public URL in the chat message.

## Setup
1. Create an R2 bucket, for example `olinam-media`.
2. Attach a public domain or r2.dev URL.
3. Deploy `workers/r2-presign.js` and bind the bucket as `R2_BUCKET`.
4. Put the worker URL in `.env` as `R2_PRESIGN_URL`.
5. Put the public base in `.env` as `R2_PUBLIC_BASE`.
6. Rebuild the app. Secrets plugin exposes these as `BuildConfig.R2_PRESIGN_URL` and `BuildConfig.R2_PUBLIC_BASE`.

## Send from code
```kotlin
MediaSender.send(
    context = context,
    repository = repository,
    conversationId = conversationId,
    uri = pickedUri,
    mediaType = MediaType.IMAGE,
    caption = "Photo"
)
```

Media types now include IMAGE, AUDIO, VOICE, VIDEO, DOCUMENT.
