package com.seifhaytham.jarvismarklx

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import org.json.JSONObject
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.io.OutputStreamWriter
import kotlin.concurrent.thread

class MainActivity : Activity() {
 private lateinit var web: WebView

 @SuppressLint("SetJavaScriptEnabled")
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  web = WebView(this)
  web.settings.javaScriptEnabled = true
  web.settings.domStorageEnabled = true
  web.settings.allowFileAccess = false
  web.webViewClient = WebViewClient()
  web.addJavascriptInterface(JarvisBridge(), "JarvisAndroid")
  setContentView(web)
  web.loadUrl("file:///android_asset/index.html")
 }

 inner class JarvisBridge {
  @JavascriptInterface
  fun askGemini(apiKey: String, prompt: String, requestId: String, model: String) {
   if (apiKey.isBlank() || prompt.isBlank()) {
    respond(requestId, null, "Add your Gemini API key and a message first.")
    return
   }
   thread {
    var connection: HttpURLConnection? = null
    try {
     val allowedModels = setOf("gemini-3.8-flash", "gemini-3.6-flash", "gemini-3.5-flash-lite", "gemini-3.1-pro-preview", "gemini-2.5-flash")
     val selectedModel = if (model in allowedModels) model else "gemini-3.8-flash"
     val endpoint = URL(
      "https://generativelanguage.googleapis.com/v1beta/models/" + selectedModel + ":generateContent?key=" +
       URLEncoder.encode(apiKey.trim(), "UTF-8")
     )
     connection = endpoint.openConnection() as HttpURLConnection
     connection.requestMethod = "POST"
     connection.connectTimeout = 20000
     connection.readTimeout = 60000
     connection.doOutput = true
     connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")

     val systemText = "You are J.A.R.V.I.S MARK LX, a capable, reliable personal AI assistant. " +
      "Reply in Egyptian Arabic when the user writes Arabic, otherwise use natural English. " +
      "Be helpful, accurate, clear, and concise. If uncertain, say so instead of inventing facts. " +
      "For complicated requests, reason carefully and give practical steps. " +
      "You cannot directly control the phone or open apps unless an Android action is implemented."
     val system = JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemText)))
     val parts = JSONArray().put(JSONObject().put("text", prompt))
     val contents = JSONArray().put(JSONObject().put("role", "user").put("parts", parts))
     val generationConfig = JSONObject().put("temperature", 0.6).put("maxOutputTokens", 2048)
     val body = JSONObject().put("systemInstruction", system).put("contents", contents)
      .put("generationConfig", generationConfig).toString()

     OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { it.write(body) }
     val code = connection.responseCode
     val stream = if (code in 200..299) connection.inputStream else connection.errorStream
     val responseText = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()

     if (code !in 200..299) {
      val apiMessage = try {
       JSONObject(responseText).getJSONObject("error").optString("message", "")
      } catch (_: Exception) { "" }
      val message = when (code) {
       400 -> "Gemini rejected the request. Check the API key and request settings. $apiMessage".trim()
       401, 403 -> "Gemini API key is invalid or does not have permission. Check your key in Google AI Studio. $apiMessage".trim()
       404 -> "The selected Gemini model ($selectedModel) is unavailable for this API key or endpoint. Try another model in the selector. $apiMessage".trim()
       429 -> "Gemini is busy or the API quota/rate limit was reached. Wait a little and try again. $apiMessage".trim()
       else -> "Gemini API error ($code). Check your internet connection and API key. $apiMessage".trim()
      }
      respond(requestId, null, message)
     } else {
      val answer = try {
       val candidates = JSONObject(responseText).optJSONArray("candidates")
       val candidate = candidates?.optJSONObject(0)
       val answerParts = candidate?.optJSONObject("content")?.optJSONArray("parts")
       val collected = StringBuilder()
       if (answerParts != null) {
        for (i in 0 until answerParts.length()) {
         val partText = answerParts.optJSONObject(i)?.optString("text", "").orEmpty()
         if (partText.isNotBlank()) {
          if (collected.isNotEmpty()) collected.append("\n")
          collected.append(partText)
         }
        }
       }
       collected.toString().ifBlank {
        val reason = candidate?.optString("finishReason", "").orEmpty()
        "Gemini returned no text. " + (if (reason.isNotBlank()) "Finish reason: $reason." else "Please try again.")
       }
      } catch (_: Exception) {
       "Gemini sent a response that could not be read. Please try again."
      }
      respond(requestId, answer, null)
     }
    } catch (e: Exception) {
     respond(requestId, null, e.message ?: "Connection failed. Check internet and API key.")
    } finally {
     connection?.disconnect()
    }
   }
  }
 }

 private fun respond(requestId: String, answer: String?, error: String?) {
  runOnUiThread {
   val payload = JSONObject().put("answer", answer ?: JSONObject.NULL)
    .put("error", error ?: JSONObject.NULL).toString()
   web.evaluateJavascript(
    "window.onGeminiResponse && window.onGeminiResponse(" +
     JSONObject.quote(requestId) + ", " + JSONObject.quote(payload) + ");",
    null
   )
  }
 }

 override fun onDestroy() {
  web.removeJavascriptInterface("JarvisAndroid")
  web.destroy()
  super.onDestroy()
 }
}