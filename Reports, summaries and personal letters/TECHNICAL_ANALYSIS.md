# 🔍 Technical Deep Dive & Challenges

This document details the technical hurdles encountered during "Operation EchoEye" and the methodologies used to overcome them. It provides a deeper look into the "Why" and "How" of the exploit chain.

## 1. Windows Exploitation: The WSUS Deserialization Challenge
### The Vulnerability (CVE-2025-59287)
The WSUS service uses `.NET Remoting` over SOAP API. When SSL is not enforced (Port 8530), the API accepts serialized objects without validation.
**The Challenge:**
Initially, attempting to generate the payload using `ysoserial` on Kali Linux failed due to Mono incompatibility with the specific gadget (`ActivitySurrogateSelector`).
**The Solution:**
We shifted the payload generation process to a Windows environment. Using `ysoserial.exe` natively on Windows allowed us to correctly serialize the PowerShell command into a SOAP-compatible format.

### The IIS Routing Issue (VHOST)
Even with a valid payload, the exploit script initially failed.
**Root Cause:** IIS servers often route traffic based on the `Host` header. Sending a request to the IP address directly resulted in a 400/404 error.
**Fix:** We manually modified the Python exploit script to inject a specific header: `Host: WIN-12GRU3SE2SU`. This tricked the IIS into routing the request to the WSUS application pool.

---

## 2. Lateral Movement: Manual Tunneling
### Why Chisel?
Standard tools like Metasploit's `autoroute` act as a layer over an existing session. However, to demonstrate a manual, stealthier approach (or when only a raw shell is available), we needed a dedicated tunneler.
**The Process:**
1.  **File Transfer:** We couldn't copy-paste to the victim machine. We spun up a Python HTTP server on Kali (`python3 -m http.server`) and used PowerShell's `wget` on the victim to pull the `chisel.exe` binary.
2.  **Reverse SOCKS Proxy:** We configured the compromised Windows server to connect *back* to us. This bypasses inbound firewall rules which usually block incoming connections but allow outgoing ones (Reverse Shell logic).

---

## 3. Android Implant: The Logic Behind SSL Bypass
### The Problem with Banking Trojans
Modern banking apps use **SSL Pinning**. This means even if an attacker installs a Root CA on the device (Man-in-the-Middle), the app will refuse to connect. Decrypting this traffic is extremely difficult.

### The "Accessibility" Workaround
Instead of attacking the network layer, we attacked the **UI Layer**.
Android's **Accessibility Services** are designed to help disabled users by reading screen content aloud.
**Our Malicious Implementation:**
* The app registers as an Accessibility Service.
* It receives a `TYPE_WINDOW_CONTENT_CHANGED` event whenever the screen updates.
* It recursively traverses the XML tree of the current window.
* It extracts the `.getText()` property from every node.
**Result:** We obtain the plaintext data (username/password) *before* it is passed to the encryption function of the banking app.

---

## 4. Post-Exploitation: The Human Element
The most critical vulnerability wasn't code, but **human error**.
While scanning the Windows server (`C:\Scripts`), we found a PowerShell backup script.
The administrator, likely trying to automate a task, hardcoded credentials in Base64 (`$AuthToken`).
**Lesson:** Security is only as strong as the weakest link. A robust firewall cannot protect against a password left in a text file.