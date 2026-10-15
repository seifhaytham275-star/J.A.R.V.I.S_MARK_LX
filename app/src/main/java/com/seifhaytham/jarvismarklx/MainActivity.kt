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
  @JavascriptInterface fun askGemini(apiKey: String, prompt: String, requestId: String) {
   if (apiKey.isBlank() || prompt.isBlank()) { respond(requestId, null, "Add your Gemini API key and a message first."); return }
   thread {
    var connection: HttpURLConnection? = null
    try {
     val endpoint = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + URLEncoder.encode(apiKey, "UTF-8"))
     connection = endpoint.openConnection() as HttpURLConnection
     connection.requestMethod = "POST"; connection.connectTimeout = 20000; connection.readTimeout = 60000; connection.doOutput = true
     connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
     val system = JSONObject().put("parts", JSONArray().put(JSONObject().put("text", "You are J.A.R.V.I.S MARK LX, a helpful, calm personal AI assistant. Reply in Egyptian Arabic when the user writes Arabic, otherwise natural English. Be accurate and concise. You cannot directly control the phone or open apps unless an Android action is implemented.")))
     val parts = JSONArray().put(JSONObject().put("text", prompt))
     val contents = JSONArray().put(JSONObject().put("role", "user").put("parts", parts))
     val body = JSONObject().put("systemInstruction", system).put("contents", contents).toString()
     OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { it.write(body) }
     val code = connection.responseCode
     val stream = if (code in 200..299) connection.inputStream else connection.errorStream
     val responseText = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
     if (code !in 200..299) {
      val message = try { JSONObject(responseText).getJSONObject("error").optString("message", "Gemini API error (" + code + ")") } catch (_: Exception) { "Gemini API error (" + code + ")" }
      respond(requestId, null, message)
     } else {
      val answer = JSONObject(responseText).getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).optString("text", "No text response.")
      respond(requestId, answer, null)
     }
    } catch (e: Exception) { respond(requestId, null, e.message ?: "Connection failed. Check internet and API key.") }
    finally { connection?.disconnect() }
   }
  }
 }
 private fun respond(requestId: String, answer: String?, error: String?) {
  runOnUiThread {
   val payload = JSONObject().put("answer", answer ?: JSONObject.NULL).put("error", error ?: JSONObject.NULL).toString()
   web.evaluateJavascript("window.onGeminiResponse && window.onGeminiResponse(" + JSONObject.quote(requestId) + ", " + JSONObject.quote(payload) + ");", null)
  }
 }
 override fun onDestroy() { web.removeJavascriptInterface("JarvisAndroid"); web.destroy(); super.onDestroy() }
}
