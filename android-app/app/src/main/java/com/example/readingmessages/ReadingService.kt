package com.example.readingmessages

import android.accessibilityservice.AccessibilityService
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Callback
import okhttp3.Call
import okhttp3.Response
import java.io.IOException
import java.util.Locale
import android.os.Handler
import android.os.Looper

class ReadingService : AccessibilityService(), TextToSpeech.OnInitListener {

    private val client = OkHttpClient()
    private val serverUrl = "http://10.100.102.50:8080/api/events"
    private lateinit var tts: TextToSpeech
    private var lastSpokenText: String = ""
    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null
    private val debounceDelay = 600L  // דיליי בין קריאות TTS (מניעת חזרות רבות מהר)

    override fun onServiceConnected() {
        super.onServiceConnected()
        tts = TextToSpeech(this, this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        Log.d("ReadingService", "Event type: ${event.eventType} pkg: ${event.packageName}")

        // הורדת הקוד שמחפש כפתור "Send" ולוחץ עליו - על פי בקשתך להסרת תכונה זו

        // איסוף הטקסט מתוך החלון הפעיל בלבד, ללא URL וכו'
        val rootNode = rootInActiveWindow ?: return
        val stringBuilder = StringBuilder()

        fun traverse(node: AccessibilityNodeInfo?, depth: Int = 0) {
            if (node == null) return
            if (depth > 20) return  // הגבלת עומק עצים
            val text = node.text
            val description = node.contentDescription
            if (!text.isNullOrEmpty()) {
                stringBuilder.append(text).append(" ")
            } else if (!description.isNullOrEmpty()) {
                stringBuilder.append(description).append(" ")
            }
            if (stringBuilder.length > 1000) return  // הגבלת אורך הטקסט המצטבר
            for (i in 0 until node.childCount) {
                traverse(node.getChild(i), depth + 1)
            }
        }

        traverse(rootNode)
        val extractedText = stringBuilder.toString().trim()

        // מניעת קריאה חוזרת של אותו טקסט
        if (extractedText.isNotEmpty() && extractedText != lastSpokenText) {
            lastSpokenText = extractedText

            // debounce לקריאה
            runnable?.let { handler.removeCallbacks(it) }
            runnable = Runnable {
                speakText(extractedText)
            }
            handler.postDelayed(runnable!!, debounceDelay)
        }

        // שליחת האירוע לשרת כמחרוזת בלבד
        val logStr = "Event: ${event.eventType} - ${event.packageName} - Text: $extractedText"
        val body = RequestBody.create("text/plain".toMediaType(), logStr)
        val request = Request.Builder().url(serverUrl).post(body).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ReadingService", "Failed to send event", e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.close()
            }
        })
    }

    private fun speakText(text: String) {
        if (this::tts.isInitialized) {
            try {
                val locale = when {
                    isHebrew(text) -> Locale("he", "IL")
                    isRussian(text) -> Locale("ru", "RU")
                    else -> Locale.US
                }
                tts.language = locale
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ttsId")
            } catch (e: Exception) {
                Log.e("ReadingService", "Error speaking text: $e")
            }
        }
    }

    private fun isHebrew(text: String): Boolean = text.any { it.code in 0x0590..0x05FF }
    private fun isRussian(text: String): Boolean = text.any { it.code in 0x0400..0x04FF }

    override fun onInterrupt() {}

    override fun onDestroy() {
        if (this::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
        }
    }
}
