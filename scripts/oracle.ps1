param([ValidateSet('setup', 'start', 'status', 'stop')][string]$Action = 'status')
$ErrorActionPreference = 'Stop'
$projectPath = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$configPath = Join-Path $projectPath '.env'

function New-LocalPassword {
    $bytes = New-Object byte[] 12
    $rng = [Security.Cryptography.RandomNumberGenerator]::Create()
    try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
    return 'Ra1_' + [BitConverter]::ToString($bytes).Replace('-', '')
}

Push-Location $projectPath
try {
    if ($Action -eq 'setup' -and -not (Test-Path -LiteralPath $configPath)) {
        $adminPassword = New-LocalPassword
        $appPassword = New-LocalPassword
        @(
            "ORACLE_ADMIN_PASSWORD=$adminPassword"
            "ORACLE_PASSWORD=$appPassword"
            'ORACLE_URL=jdbc:oracle:thin:@localhost:1522/FREEPDB1'
            'ORACLE_USERNAME=rachai'
            'SERVER_PORT=8081'
        ) | Set-Content -LiteralPath $configPath -Encoding ascii
        Write-Output 'Credenciais novas salvas somente no .env local (ignorado pelo Git).'
    }
    if (-not (Test-Path -LiteralPath $configPath)) { throw 'Execute primeiro: .\scripts\oracle.ps1 setup' }
    switch ($Action) {
        'setup' { & docker compose up -d oracle }
        'start' { & docker compose start oracle }
        'status' { & docker compose ps oracle }
        'stop' { & docker compose stop oracle }
    }
    if ($LASTEXITCODE -ne 0) { throw 'A operação Docker falhou; confira o erro acima.' }
} finally { Pop-Location }
