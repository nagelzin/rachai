# Executar somente no contêiner próprio do Rachaí, após ficar healthy.
# Não altera schema existente e nunca redefine senhas.
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'load-local-env.ps1')
if ($env:ORACLE_PASSWORD -notmatch '^[A-Za-z0-9_]{12,30}$') {
    throw 'A senha local deve ter 12 a 30 letras, números ou sublinhados. Use a senha gerada no setup.'
}
$projectPath = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$owner = & docker inspect --format '{{index .Config.Labels "com.docker.compose.project"}}' rachai-oracle
if ($LASTEXITCODE -ne 0 -or $owner.Trim() -ne 'rachai') { throw 'O contêiner não pertence ao projeto rachai.' }
$health = & docker inspect --format '{{.State.Health.Status}}' rachai-oracle
if ($LASTEXITCODE -ne 0 -or $health.Trim() -ne 'healthy') { throw 'Aguarde o Oracle ficar healthy antes de criar o schema.' }

$ddl = Get-Content -LiteralPath (Join-Path $projectPath 'database\ddl.sql') -Raw -Encoding utf8
$seed = Get-Content -LiteralPath (Join-Path $projectPath 'database\inserts.sql') -Raw -Encoding utf8
$migration = (Get-ChildItem -LiteralPath (Join-Path $projectPath 'database\migrations') -Filter '*.sql' | Sort-Object Name | ForEach-Object { Get-Content -LiteralPath $_.FullName -Raw -Encoding utf8 }) -join "`n"
$sql = @"
set echo off verify off define off feedback off
whenever oserror exit failure rollback
whenever sqlerror exit sql.sqlcode rollback
alter session set container=FREEPDB1;
declare
    quantidade number;
begin
    select count(*) into quantidade from dba_users where username = 'RACHAI';
    if quantidade > 0 then
        raise_application_error(-20001, 'RACHAI ja existe. Nenhuma alteracao foi realizada.');
    end if;
    select count(*) into quantidade from dba_tablespaces where tablespace_name = 'RACHAI_DATA';
    if quantidade = 0 then
        execute immediate 'create tablespace RACHAI_DATA datafile ''/opt/oracle/oradata/rachai_data01.dbf'' size 20M autoextend on next 10M maxsize 100M';
    end if;
end;
/
create user RACHAI identified by "$env:ORACLE_PASSWORD" default tablespace RACHAI_DATA temporary tablespace TEMP quota 100M on RACHAI_DATA;
grant create session, create table to RACHAI;
alter session set current_schema=RACHAI;
$ddl
$seed
$migration
select table_name from all_tables where owner='RACHAI' order by table_name;
exit
"@
$previousEncoding = $OutputEncoding
try {
    $OutputEncoding = New-Object System.Text.UTF8Encoding
    $result = $sql | & docker exec -i -e NLS_LANG=.AL32UTF8 rachai-oracle sqlplus -s / as sysdba 2>&1
    $sqlExitCode = $LASTEXITCODE
    # Até um erro SQL pode repetir a instrução: remover segredos antes de exibir.
    foreach ($line in $result) {
        $safeLine = ([string]$line).Replace($env:ORACLE_PASSWORD, '[senha omitida]')
        Write-Output $safeLine
    }
    if ($sqlExitCode -ne 0) { throw 'A criação do schema falhou. Inspecione o banco antes de repetir; DDL pode ter sido aplicado parcialmente.' }
    Write-Output 'Schema RACHAI criado. Base e migrações preparadas.'
} finally {
    $OutputEncoding = $previousEncoding
    $sql = $null
}
