/**
 * Cloudflare Worker for Olinam App: R2 Media & Profile DP Storage.
 * Handles presigned upload requests, direct streaming PUT uploads, CORS,
 * and high-speed edge delivery with 0 egress cost.
 *
 * wrangler.toml:
 *   name = "olinam-r2-media"
 *   main = "workers/r2-presign.js"
 *   compatibility_date = "2024-01-01"
 *
 *   [[r2_buckets]]
 *   binding = "R2_BUCKET"
 *   bucket_name = "olinam-media"
 *
 *   [vars]
 *   PUBLIC_BASE = "" # Optional: If empty, defaults to worker URL
 */

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET, HEAD, POST, PUT, OPTIONS",
  "Access-Control-Allow-Headers": "*",
  "Access-Control-Max-Age": "86400",
};

export default {
  async fetch(request, env) {
    const url = new URL(request.url);

    // 1. Handle CORS Preflight
    if (request.method === "OPTIONS") {
      return new Response(null, { headers: corsHeaders });
    }

    // 2. Step 1: POST - Request upload URL
    if (request.method === "POST") {
      try {
        const body = await request.json();
        const key = body.key;
        const contentType = body.contentType || "application/octet-stream";

        if (!key || key.includes("..")) {
          return new Response(JSON.stringify({ error: "Invalid object key" }), {
            status: 400,
            headers: { ...corsHeaders, "Content-Type": "application/json" }
          });
        }

        const publicBase = env.PUBLIC_BASE?.trim() || `${url.origin}/file`;
        const uploadUrl = `${url.origin}/upload?key=${encodeURIComponent(key)}&contentType=${encodeURIComponent(contentType)}`;

        return new Response(
          JSON.stringify({
            uploadUrl: uploadUrl,
            publicUrl: `${publicBase}/${key}`,
            key: key
          }),
          {
            status: 200,
            headers: { ...corsHeaders, "Content-Type": "application/json" }
          }
        );
      } catch (err) {
        return new Response(JSON.stringify({ error: err.message }), {
          status: 400,
          headers: { ...corsHeaders, "Content-Type": "application/json" }
        });
      }
    }

    // 3. Step 2: PUT - Direct binary stream upload to Cloudflare R2
    if (request.method === "PUT") {
      const key = url.searchParams.get("key");
      const contentType = request.headers.get("Content-Type") ||
                          url.searchParams.get("contentType") ||
                          "application/octet-stream";

      if (!key) {
        return new Response("Missing key parameter", { status: 400, headers: corsHeaders });
      }

      await env.R2_BUCKET.put(key, request.body, {
        httpMetadata: { contentType: contentType }
      });

      return new Response(JSON.stringify({ success: true, key }), {
        status: 200,
        headers: { ...corsHeaders, "Content-Type": "application/json" }
      });
    }

    // 4. GET /file/:key or GET /:key - Serve public media directly with edge caching
    if (request.method === "GET" || request.method === "HEAD") {
      let key = url.pathname.replace(/^\/file\//, "").replace(/^\//, "");
      if (!key) {
        return new Response("Olinam Cloudflare R2 Media Edge Service is Running.", {
          headers: { ...corsHeaders, "Content-Type": "text/plain" }
        });
      }

      const object = await env.R2_BUCKET.get(key);
      if (!object) {
        return new Response("Object Not Found", { status: 404, headers: corsHeaders });
      }

      const headers = new Headers();
      object.writeHttpMetadata(headers);
      headers.set("etag", object.httpEtag);
      headers.set("Access-Control-Allow-Origin", "*");
      headers.set("Cache-Control", "public, max-age=31536000, immutable");

      return new Response(object.body, { headers });
    }

    return new Response("Method Not Allowed", { status: 405, headers: corsHeaders });
  }
};
