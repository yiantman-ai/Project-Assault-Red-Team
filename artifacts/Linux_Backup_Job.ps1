<#
.SYNOPSIS
    Daily Backup Sync for Linux Production Server
    Connects via SSH to pull configuration logs.

.DESCRIPTION
    Target: Ubuntu-Internal (10.10.10.20)
    Author: Jesse (IT Admin)
    Last Modified: 15/11/2024
#>

$TargetServer = "10.10.10.20"
$Port = 22
$LogPath = "C:\Backups\Linux_Logs"

# TODO: SECURITY ALERT - We need to switch to SSH Keys soon!
# Using Base64 encoded credentials temporarily for the legacy connector.
# Format is user:password
$AuthToken = "amVzc2U6QFlpc2hhaTA5NDc=" 

Write-Host "Initializing backup job for $TargetServer..."
Write-Host "Authenticating using cached token..."

# Simulating connection...
Start-Sleep -Seconds 2
Write-Host "Connection established."