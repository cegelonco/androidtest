# Run this if Gradle reports corrupted transforms / metadata.bin.
# Close Android Studio first, then run: powershell -ExecutionPolicy Bypass -File .\clean-gradle-cache.ps1

$ErrorActionPreference = "Stop"
$jbr = "${env:ProgramFiles}\Android\Android Studio\jbr\bin\java.exe"
if (Test-Path $jbr) {
  $env:JAVA_HOME = Split-Path (Split-Path $jbr)
  $env:Path = "$(Split-Path $jbr);$env:Path"
  Set-Location $PSScriptRoot
  & .\gradlew.bat --stop
}

$caches = "$env:USERPROFILE\.gradle\caches"
if (Test-Path $caches) {
  Remove-Item -LiteralPath $caches -Recurse -Force
  Write-Host "Removed: $caches"
}

$local = Join-Path $PSScriptRoot ".gradle"
if (Test-Path $local) {
  Remove-Item -LiteralPath $local -Recurse -Force
  Write-Host "Removed: $local"
}

Write-Host "Done. Reopen Android Studio and Sync Gradle."
