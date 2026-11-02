param([ValidateSet('dev', 'check', 'install')][string]$Action = 'dev')
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'use-local-tools.ps1')
$nodeVersion = & node -p 'process.versions.node'
if ($LASTEXITCODE -ne 0 -or [version]$nodeVersion -lt [version]'22.12.0') { throw 'Instale Node 24 LTS (mínimo 22.12) ou use a cópia portátil em .tools.' }
Push-Location (Join-Path $PSScriptRoot '..\frontend')
try {
    if ($Action -eq 'install') {
        & npm.cmd ci
        if ($LASTEXITCODE -ne 0) { throw 'Falha ao instalar as dependências.' }
    } elseif ($Action -eq 'check') {
        foreach ($step in @('lint', 'test', 'build', 'format:check')) {
            & npm.cmd run $step
            if ($LASTEXITCODE -ne 0) { throw "Falha em npm run $step" }
        }
    } else {
        & npm.cmd run dev
        if ($LASTEXITCODE -ne 0) { throw 'Falha ao iniciar o frontend.' }
    }
} finally { Pop-Location }
