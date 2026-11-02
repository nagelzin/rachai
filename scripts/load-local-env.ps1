# Carrega apenas as variáveis locais esperadas, sem executar o conteúdo do arquivo.
$localEnvPath = Join-Path $PSScriptRoot '..\.env'
$localEnvKeys = @('ORACLE_PASSWORD', 'ORACLE_URL', 'ORACLE_USERNAME', 'SERVER_PORT')
if (Test-Path -LiteralPath $localEnvPath) {
    foreach ($localEnvLine in Get-Content -LiteralPath $localEnvPath) {
        if ($localEnvLine -match '^([A-Z_]+)=(.*)$' -and $Matches[1] -in $localEnvKeys) {
            # Configuração explícita do terminal prevalece sobre o arquivo.
            if (-not [Environment]::GetEnvironmentVariable($Matches[1], 'Process')) {
                [Environment]::SetEnvironmentVariable($Matches[1], $Matches[2], 'Process')
            }
        }
    }
}
