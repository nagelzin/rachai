# Dot-source para ativar ferramentas apenas neste terminal, sem mudar o Windows.
$toolsPath = Join-Path $PSScriptRoot '..\.tools'
$localNode = Get-ChildItem -LiteralPath $toolsPath -Directory -Filter 'node-v24.*-win-x64' -ErrorAction SilentlyContinue | Sort-Object Name -Descending | Select-Object -First 1
if ($localNode) { $env:PATH = $localNode.FullName + ';' + $env:PATH }
if (-not $env:JAVA_HOME -and (Test-Path 'C:\Program Files\Java\jdk-21\bin\javac.exe')) {
    $env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
}
if ($env:JAVA_HOME) { $env:PATH = (Join-Path $env:JAVA_HOME 'bin') + ';' + $env:PATH }
