# Migração incremental somente do contêiner local do Rachaí.
$ErrorActionPreference = 'Stop'
$projectPath = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$owner = & docker inspect --format '{{index .Config.Labels "com.docker.compose.project"}}' rachai-oracle
if ($LASTEXITCODE -ne 0 -or $owner.Trim() -ne 'rachai') { throw 'Contêiner rachai-oracle indisponível ou de outro projeto.' }
$health = & docker inspect --format '{{.State.Health.Status}}' rachai-oracle
if ($LASTEXITCODE -ne 0 -or $health.Trim() -ne 'healthy') { throw 'Aguarde o Oracle ficar healthy.' }
$migration = (Get-ChildItem -LiteralPath (Join-Path $projectPath 'database\migrations') -Filter '*.sql' | Sort-Object Name | ForEach-Object { Get-Content -LiteralPath $_.FullName -Raw -Encoding utf8 }) -join "`n"
$sql = "set echo off verify off define off`nwhenever sqlerror exit failure rollback`nalter session set container=FREEPDB1;`nalter session set current_schema=RACHAI;`n$migration`nexit"
$previousEncoding = $OutputEncoding
try {
    $OutputEncoding = [Text.UTF8Encoding]::new($false)
    $sql | & docker exec -i -e NLS_LANG=.AL32UTF8 rachai-oracle sqlplus -s / as sysdba
    if ($LASTEXITCODE -ne 0) { throw 'Migração falhou; inspecione o erro. O script permite retomar DDL parcial.' }
    Write-Output 'Migrações aplicadas em ordem; registros anteriores preservados.'
} finally { $OutputEncoding = $previousEncoding }
