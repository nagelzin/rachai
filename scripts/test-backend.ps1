$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'use-local-tools.ps1')
. (Join-Path $PSScriptRoot 'load-local-env.ps1')
Push-Location (Join-Path $PSScriptRoot '..\backend')
try {
    & .\mvnw.cmd verify
    if ($LASTEXITCODE -ne 0) { throw 'Falha na verificação do backend.' }
} finally { Pop-Location }
