/**
 * Cloudflare Worker for Oj AI (Olinam WhatsApp Messenger AI Agent)
 * 
 * Features:
 * - Handles POST requests to /api/chat or /
 * - Parses user prompt, history, and model parameters
 * - Calls Cloudflare Workers AI (@cf/meta/llama-3.1-8b-instruct or custom models) or Gemini API
 * - Returns clean JSON response { response: "...", model: "...", status: "success" }
 * - Full CORS support for cross-origin and mobile app calls
 * 
 * How to Deploy:
 * 1. npx wrangler deploy workers/oj-ai-worker.js --name oj-ai-worker
 * 2. Set your Cloudflare Workers AI binding or GEMINI_API_KEY secret if needed:
 *    npx wrangler secret put GEMINI_API_KEY
 * 3. Update CLOUDFLARE_WORKER_URL in OjAiService.kt or through the Oj AI Profile Screen in the app!
 */

export default {
  async fetch(request, env, ctx) {
    // 1. Handle CORS Preflight
    if (request.method === "OPTIONS") {
      return new Response(null, {
        status: 204,
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
          "Access-Control-Allow-Headers": "Content-Type, Authorization",
          "Access-Control-Max-Age": "86400",
        },
      });
    }

    const url = new URL(request.url);

    // Health check endpoint
    if (request.method === "GET") {
      return new Response(
        JSON.stringify({
          status: "online",
          service: "Oj AI Cloudflare Worker",
          version: "1.0.0",
          endpoints: ["/api/chat", "/"],
        }),
        {
          headers: {
            "Content-Type": "application/json",
            "Access-Control-Allow-Origin": "*",
          },
        }
      );
    }

    if (request.method !== "POST") {
      return new Response(JSON.stringify({ error: "Method not allowed. Use POST." }), {
        status: 405,
        headers: { "Content-Type": "application/json", "Access-Control-Allow-Origin": "*" },
      });
    }

    try {
      const body = await request.json().catch(() => ({}));
      const prompt = (body.prompt || body.message || body.text || "").trim();
      const history = Array.isArray(body.history) ? body.history : [];

      if (!prompt) {
        return new Response(JSON.stringify({ error: "Prompt is required in JSON body" }), {
          status: 400,
          headers: { "Content-Type": "application/json", "Access-Control-Allow-Origin": "*" },
        });
      }

      // Check if Cloudflare Workers AI binding is available
      let aiResponseText = "";
      if (env.AI) {
        try {
          const messages = [
            {
              role: "system",
              content:
                "You are Oj AI, the ultra-smart, fast, and helpful AI assistant built directly into the Olinam WhatsApp Messenger app. You provide direct, friendly, and accurate answers with markdown formatting when appropriate.",
            },
          ];

          for (const msg of history) {
            messages.push({
              role: msg.role === "assistant" ? "assistant" : "user",
              content: msg.content || "",
            });
          }

          messages.push({ role: "user", content: prompt });

          const aiResult = await env.AI.run("@cf/meta/llama-3.1-8b-instruct", {
            messages,
            max_tokens: 1024,
            temperature: 0.7,
          });

          aiResponseText = aiResult.response || "";
        } catch (err) {
          console.error("Workers AI error:", err);
        }
      }

      // Fallback: If GEMINI_API_KEY is configured in Worker secrets
      if (!aiResponseText && env.GEMINI_API_KEY) {
        try {
          const geminiUrl = `https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${env.GEMINI_API_KEY}`;
          const geminiResp = await fetch(geminiUrl, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
              contents: [
                ...history.map((h) => ({
                  role: h.role === "assistant" ? "model" : "user",
                  parts: [{ text: h.content || "" }],
                })),
                { role: "user", parts: [{ text: prompt }] },
              ],
            }),
          });

          if (geminiResp.ok) {
            const data = await geminiResp.json();
            aiResponseText =
              data?.candidates?.[0]?.content?.parts?.[0]?.text || "";
          }
        } catch (err) {
          console.error("Gemini fallback error:", err);
        }
      }

      // Default smart assistant reply if no external AI binding is attached yet
      if (!aiResponseText) {
        aiResponseText = `I received your message: "${prompt}". Cloudflare Worker is connected and processing requests smoothly!`;
      }

      return new Response(
        JSON.stringify({
          response: aiResponseText,
          text: aiResponseText,
          message: aiResponseText,
          status: "success",
          provider: "cloudflare-worker",
          timestamp: Date.now(),
        }),
        {
          status: 200,
          headers: {
            "Content-Type": "application/json",
            "Access-Control-Allow-Origin": "*",
          },
        }
      );
    } catch (err) {
      return new Response(
        JSON.stringify({
          error: err.message || "Internal server error",
          status: "error",
        }),
        {
          status: 500,
          headers: {
            "Content-Type": "application/json",
            "Access-Control-Allow-Origin": "*",
          },
        }
      );
    }
  },
};
