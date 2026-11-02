# Executar a partir de qualquer diretório. Requer JDK 21 em JAVA_HOME.
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'use-local-tools.ps1')
. (Join-Path $PSScriptRoot 'load-local-env.ps1')
$backendPath = Join-Path $PSScriptRoot '..\backend'
if (-not $env:JAVA_HOME -or -not (Test-Path (Join-Path $env:JAVA_HOME 'bin\javac.exe'))) {
    throw 'Configure JAVA_HOME apontando para um JDK 21 antes de iniciar.'
}
if (-not $env:ORACLE_PASSWORD) {
    $securePassword = Read-Host 'Senha do schema Oracle Rachaí' -AsSecureString
    $env:ORACLE_PASSWORD = [System.Net.NetworkCredential]::new('', $securePassword).Password
    $temporaryPassword = $true
}
Push-Location $backendPath
try {
    & .\mvnw.cmd spring-boot:run
    if ($LASTEXITCODE -ne 0) { throw 'O backend não iniciou. Confira o Oracle e o log acima.' }
} finally {
    Pop-Location
    if ($temporaryPassword) { Remove-Item Env:\ORACLE_PASSWORD }
}
