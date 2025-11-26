👁️ EchoEye APT Simulation: Full Kill Chain
Advanced Red Team Operation: From Web Breach to Mobile Data Exfiltration
⚠️ Disclaimer
This project was created for educational purposes and academic research only. The techniques demonstrated here (SQL Injection, WSUS Exploitation, Accessibility Abuse) are intended to highlight security vulnerabilities in hybrid environments. Misuse of this code is strictly prohibited.

📖 Project Overview
This project demonstrates a full Advanced Persistent Threat (APT) simulation on a fictional organization ("EchoEye"). The attack lifecycle follows a complete Kill Chain:

Initial Access: Bypassing authentication via SQL Injection.

Privilege Escalation: Compromising a Domain Controller via WSUS Vulnerability (CVE-2025-59287).

Lateral Movement: Pivoting to an isolated internal Linux network using Chisel.

Data Exfiltration: Deploying Android spyware to steal financial data in real-time.

🗺️ Attack Flow & Methodology
Phase 1: Reconnaissance & SQL Injection
Target: EchoEye Corporate Portal (Port 8080).

Vulnerability: The login form failed to sanitize inputs.

Exploit: Injected ' OR 1=1-- into the username field.

Result: Authentication bypass revealing internal infrastructure details (WSUS configuration).

Phase 2: Infrastructure Compromise (Windows Server 2022)
Scanning: nmap revealed open ports:

1433 (SQL Server)

8530 (WSUS - Unencrypted HTTP)

Vulnerability (CVE-2025-59287): The WSUS service allowed unauthenticated SOAP requests via ClientWebService/client.asmx.

Exploitation:

Developed a custom Python script (wsus_poc.py) to inject a malicious serialized object.

Payload generated manually using ysoserial.exe (ActivitySurrogateSelector gadget).

Critical Configuration: Set Host: WIN-12GRU3SE2SU header to bypass IIS IP filtering.

Privilege Escalation: Escalated from Network Service to SYSTEM using Named Pipe Impersonation.

Phase 3: Lateral Movement (The Pivot)
Discovery: ipconfig revealed a hidden internal interface (10.10.10.10).

Tunneling: Established a SOCKS5 Reverse Tunnel using Chisel to route traffic from Kali into the internal network.

Credential Harvesting:

Found a forgotten script: C:\Scripts\Linux_Backup_Job.ps1.

Decoded Base64 token found inside: amVzc2U6QFlpc2hhaTA5NDc= -> jesse:@Yishai0947.

Phase 4: Linux Takeover & C2 Setup
Access: Connected via SSH tunnel (proxychains) to the internal Ubuntu server (10.10.10.20).

Privilege Escalation: Abused misconfigured Sudo rights ((ALL : ALL) ALL) to gain Root access (sudo su -).

C2 Deployment: Deployed a custom Python Flask server (reading_server) to listen for incoming data.

Phase 5: Android Espionage (The Endgame)
Implant: Custom Android app ("ReadingMessages") abusing Accessibility Services to read screen content.

Data Theft: Intercepted Bank Hapoalim login credentials (Username/Password) in Cleartext, bypassing SSL pinning by scraping the UI layer.

Active Control: Sent a remote command from the C2 server injecting a fake alert: "YOU HAVE BEEN HACKED BY BIG POTCUS".

📸 Proof of Concept (Screenshots)
1. SQL Injection & Initial Breach
Bypassing the login screen using SQLi payload.

2. The "Smoking Gun" - Banking Data Exfiltration
Intercepting credentials in real-time logs (Bypassing HTTPS).

3. Active C2 - Remote Command Execution
Injecting the "BIG POTCUS" alert to the victim's screen.

🛠️ Repository Structure
/c2_server - Python Flask server (app.py) for handling stolen data & sending commands.

/exploits - Automation scripts (wsus_poc.py, chisel) for the Windows breach.

/artifacts - The compromised PowerShell script (Linux_Backup_Job.ps1) found on the server.

/android-app - Source code for the malicious Android implant.

👨‍💻 Author
Jesse Antman - Red Team Researcher