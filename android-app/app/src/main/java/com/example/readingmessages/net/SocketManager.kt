package com.example.app.net

import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import java.net.URISyntaxException

class SocketManager(
    private val baseUrl: String,
    private val deviceId: String
) {
    private var socket: Socket? = null

    interface Listener {
        fun onConnected()
        fun onDisconnected()
        fun onServerCommand(command: String, payload: JSONObject?)
        fun onError(t: Throwable)
    }

    var listener: Listener? = null

    fun connect() {
        if (socket != null && socket!!.connected()) return
        try {
            val opts = IO.Options().apply {
                transports = arrayOf("websocket")
                reconnection = true
                reconnectionAttempts = Int.MAX_VALUE
                reconnectionDelay = 1000
                query = "deviceId=$deviceId"
                forceNew = true
            }
            socket = IO.socket("$baseUrl/device", opts)
        } catch (e: URISyntaxException) {
            listener?.onError(e)
            return
        }

        socket!!.on(Socket.EVENT_CONNECT, Emitter.Listener {
            listener?.onConnected()
            val hello = JSONObject(mapOf("hello" to "from Android", "deviceId" to deviceId))
            socket!!.emit("client_event", hello)
        })

        socket!!.on("server_command", Emitter.Listener { args ->
            val data = args.firstOrNull() as? JSONObject
            val cmd = data?.optString("command").orEmpty()
            listener?.onServerCommand(cmd, data)
            socket!!.emit("server_ack", JSONObject(mapOf("cmd" to cmd, "ok" to true)))
        })

        socket!!.on(Socket.EVENT_DISCONNECT, Emitter.Listener {
            listener?.onDisconnected()
        })

        socket!!.on(Socket.EVENT_CONNECT_ERROR, Emitter.Listener { args ->
            val t = (args.firstOrNull() as? Exception) ?: RuntimeException("connect error")
            listener?.onError(t)
        })

        socket!!.connect()
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
    }
}
