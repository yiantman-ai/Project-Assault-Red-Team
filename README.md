# 👁️ EchoEye APT Simulation: Full Kill Chain
### Advanced Red Team Operation: From Web Breach to Mobile Data Exfiltration

![CyberSecurity](https://img.shields.io/badge/Cyber-Security-red) ![Python](https://img.shields.io/badge/Python-3.x-blue) ![Android](https://img.shields.io/badge/Platform-Android-green) ![Exploit](https://img.shields.io/badge/Vulnerability-SQLi%20%26%20RCE-critical)

## ⚠️ Disclaimer
> **This project was created for educational purposes and academic research only.**
> The techniques demonstrated here (SQL Injection, WSUS Exploitation, Accessibility Abuse) are intended to highlight security vulnerabilities in hybrid environments. Misuse of this code is strictly prohibited.

---

## 📖 Project Overview
This project demonstrates a full **Advanced Persistent Threat (APT)** simulation on a fictional organization ("EchoEye").
The attack lifecycle follows a complete Kill Chain:
1.  **Initial Access:** Bypassing authentication via **SQL Injection**.
2.  **Privilege Escalation:** Compromising a Domain Controller via **WSUS Vulnerability (CVE-2025-59287)**.
3.  **Lateral Movement:** Pivoting to an isolated internal Linux network.
4.  **Data Exfiltration:** Deploying Android spyware to steal financial data in real-time.

---

## 🗺️ Attack Flow & Methodology

### Phase 1: Reconnaissance & SQL Injection
* **Target:** EchoEye Corporate Portal (Port 8080).
* **Vulnerability:** The login form failed to sanitize inputs.
* **Exploit:** Injected `' or 1=1--` into the username field.
* **Result:** Authentication bypass revealing internal infrastructure details (WSUS configuration).

### Phase 2: Infrastructure Compromise (Windows Server 2022)
* **Scanning:** `nmap -A -sV -sC -p- -O -T4` revealed open ports:
    * **1433** (SQL Server)
    * **8530** (WSUS - Unencrypted HTTP)
* **Vulnerability (CVE-2025-59287):** The WSUS service allowed unauthenticated SOAP requests via `ClientWebService/client.asmx`.
* **Exploitation:**
    * Used Metasploit module: `exploit/windows/http/wsus_deserialization_rce`.
    * **Critical Configuration:** Set `VHOST` to the server name (`WIN-12GRU3SE2SU`) to bypass IIS routing issues.
    * **Privilege Escalation:** Escalated from `Network Service` to **SYSTEM** using Named Pipe Impersonation.

### Phase 3: Lateral Movement (The Pivot)
* **Discovery:** `ipconfig` revealed a hidden internal interface (`10.10.10.10`).
* **Tunneling:** Established a SOCKS proxy/Port Forwarding via Meterpreter (`portfwd add -l 2222 -p 22 -r 10.10.10.20`).
* **Credential Harvesting:**
    * Found a forgotten script: `C:\Scripts\Linux_Backup_Job.ps1`.
    * Decoded Base64 token found inside: `amVzc2U6QFlpc2hhaTA5NDc=` -> **`jesse:@Yishai0947`**.

### Phase 4: Linux Takeover & C2 Setup
* **Access:** Connected via SSH tunnel to the internal Ubuntu server (`10.10.10.20`).
* **Privilege Escalation:** Abused misconfigured Sudo rights (`(ALL : ALL) ALL`) to gain **Root** access (`sudo su -`).
* **C2 Deployment:** Deployed a custom Python Flask server (`reading_server`) to listen for incoming data.

### Phase 5: Android Espionage (The Endgame)
* **Implant:** Custom Android app abusing **Accessibility Services** to read screen content.
* **Data Theft:** Intercepted **Bank Hapoalim** login credentials (Username/Password) in Cleartext, bypassing SSL pinning.
* **Active Control:** Sent a remote command from the C2 server injecting a fake alert: **"YOU HAVE BEEN HACKED BY BIG POTCUS"**.

---

## 📸 Proof of Concept (Screenshots)

### 1. SQL Injection & Initial Breach
*Bypassing the login screen using SQLi payload:*

<img width="865" height="36" alt="image" src="https://github.com/user-attachments/assets/f9bf91bc-c7e4-4e8f-817a-0439aa903935" />

### 2. The "Smoking Gun" - Banking Data Exfiltration
*Intercepting credentials in real-time logs (Bypassing HTTPS):*

<img width="865" height="17" alt="image" src="https://github.com/user-attachments/assets/e21d7d4d-117a-4edc-844e-ebb096eff4ba" />

### 3. Active C2 - Remote Command Execution
*Injecting the "BIG POTCUS" alert to the victim's screen:*

<img width="509" height="1083" alt="image" src="https://github.com/user-attachments/assets/c93935ec-0fa9-4de1-a034-d885daf8ddb6" />

---

## 🛠️ Repository Structure

* **`/c2_server`** - Python Flask server for handling stolen data & sending commands.
* **`/exploits`** - Automation scripts (`wsus_dc_exploit.py`, `launch_attack.sh`) for the Windows breach.
* **`/artifacts`** - The compromised PowerShell script (`Linux_Backup_Job.ps1`) found on the server.
* **`/android-app`** - Source code for the malicious Android implant.

---

### 👨‍💻 Author
**Jesse Antman** - Red Team Researcher
