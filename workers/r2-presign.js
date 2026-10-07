/**
 * Cloudflare Worker: issues a one-time R2 PUT URL.
 * Bind an R2 bucket as R2_BUCKET and set PUBLIC_BASE.
 *
 * wrangler.toml:
 *   name = "olinam-r2-presign"
 *   main = "workers/r2-presign.js"
 *   [[r2_buckets]]
 *   binding = "R2_BUCKET"
 *   bucket_name = "olinam-media"
 *   [vars]
 *   PUBLIC_BASE = "https://media.yourdomain.com"
 */
export default {
  async fetch(request, env) {
    if (request.method !== "POST") {
      return new Response("POST only", { status: 405 });
    }
    const body = await request.json();
    const key = body.key;
    const contentType = body.contentType || "application/octet-stream";
    if (!key || key.includes("..")) {
      return new Response("invalid key", { status: 400 });
    }
    const uploadUrl = await env.R2_BUCKET.createPresignedUrl({
      method: "PUT",
      key,
      expiresIn: 600,
      headers: { "content-type": contentType }
    });
    return Response.json({
      uploadUrl,
      publicUrl: `${env.PUBLIC_BASE}/${key}`
    });
  }
};
