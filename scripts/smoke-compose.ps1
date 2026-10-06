param(
    [string]$RuntimeImage = 'delivery-backend:mvp',
    [string]$Docker = 'docker'
)
$ErrorActionPreference = 'Stop'
$project = 'delivery-compose-smoke-' + [guid]::NewGuid().ToString('N').Substring(0,12)
$volume = "$project-data"
$root = Split-Path -Parent $PSScriptRoot
$overlayPath = Join-Path ([IO.Path]::GetTempPath()) "$project.json"
$envPath = Join-Path ([IO.Path]::GetTempPath()) "$project.env"
$compose = @('compose','--project-name',$project,'--env-file',$envPath,'-f',(Join-Path $root 'compose.yaml'),'-f',$overlayPath)
$variables = @{
    DB_NAME='delivery_smoke'; DB_USER='delivery'; DB_PASSWORD=[guid]::NewGuid().ToString('N')
    JWT_SECRET=([guid]::NewGuid().ToString('N') + [guid]::NewGuid().ToString('N'))
    PORT='0'; TRUST_PROXY='false'
}
$previous = @{}
function Invoke-Compose {
    param([string[]]$Arguments)
    $result = & $Docker @compose @Arguments
    if ($LASTEXITCODE -ne 0) { throw "Compose failed: $($Arguments[0])" }
    return $result
}
function Expect-Status {
    param($Response, [int]$Expected)
    if ([int]$Response.StatusCode -ne $Expected) { throw "Expected HTTP $Expected, got $($Response.StatusCode)" }
}
try {
    foreach ($name in $variables.Keys) {
        $previous[$name] = [Environment]::GetEnvironmentVariable($name,'Process')
        [Environment]::SetEnvironmentVariable($name,$variables[$name],'Process')
    }
    # An explicit empty env file prevents loading the development .env.
    [IO.File]::WriteAllText($envPath,'')
    $overlay = @{
        services = @{ backend = @{ image=$RuntimeImage; pull_policy='never' } }
        volumes = @{ postgres_data = @{ name=$volume } }
    } | ConvertTo-Json -Depth 8
    [IO.File]::WriteAllText($overlayPath,$overlay)
    $config = (Invoke-Compose -Arguments @('config','--format','json') | Out-String) | ConvertFrom-Json
    if ($config.volumes.postgres_data.name -ne $volume) { throw 'Unsafe volume configuration' }
    if ($config.services.backend.ports[0].host_ip -ne '127.0.0.1' -or $config.services.backend.ports[0].published -ne '0') {
        throw 'Expected an ephemeral loopback port'
    }
    Invoke-Compose -Arguments @('up','-d','--no-build','--wait','--wait-timeout','120') | Out-Null
    $binding = (Invoke-Compose -Arguments @('port','backend','8080')).Trim()
    if ($binding -notmatch '^127\.0\.0\.1:(\d+)$') { throw 'Unexpected port binding' }
    $baseUrl = "http://127.0.0.1:$($Matches[1])"
    Expect-Status (Invoke-WebRequest "$baseUrl/ready" -TimeoutSec 20) 200
    Expect-Status (Invoke-WebRequest "$baseUrl/swagger" -TimeoutSec 20) 200
    $body = @{email='compose@example.com';phone='+5355550101';password='ComposeSmoke123!';firstName='Compose';lastName='Smoke'} | ConvertTo-Json
    $registered = Invoke-WebRequest "$baseUrl/auth/register" -Method Post -ContentType 'application/json' -Body $body -SkipHttpErrorCheck -TimeoutSec 30
    Expect-Status $registered 201
    $session = $registered.Content | ConvertFrom-Json
    Invoke-Compose -Arguments @('restart','backend') | Out-Null
    Invoke-Compose -Arguments @('up','-d','--no-build','--wait','--wait-timeout','120') | Out-Null
    # Docker may assign a different ephemeral host port after restart.
    $binding = (Invoke-Compose -Arguments @('port','backend','8080')).Trim()
    if ($binding -notmatch '^127\.0\.0\.1:(\d+)$') { throw 'Unexpected port binding after restart' }
    $baseUrl = "http://127.0.0.1:$($Matches[1])"
    $me = Invoke-RestMethod "$baseUrl/auth/me" -Headers @{Authorization="Bearer $($session.accessToken)"} -TimeoutSec 20
    if ($me.id -ne $session.userId) { throw 'User did not persist across restart' }
    $migrations = (Invoke-Compose -Arguments @('exec','-T','postgres','psql','-U','delivery','-d','delivery_smoke','-Atc','SELECT count(*) FROM flyway_schema_history WHERE success')).Trim()
    if ($migrations -ne '6') { throw 'Expected six successful migrations' }
    Invoke-Compose -Arguments @('stop','postgres') | Out-Null
    Expect-Status (Invoke-WebRequest "$baseUrl/ready" -SkipHttpErrorCheck -TimeoutSec 20) 503
    Expect-Status (Invoke-WebRequest "$baseUrl/live" -TimeoutSec 20) 200
    Write-Output 'COMPOSE SMOKE PASSED: healthy services, migrations, registration, restart persistence, database outage and liveness.'
} finally {
    if (Test-Path -LiteralPath $overlayPath) {
        # Only this invocation's GUID-named project and volume are removed.
        Invoke-Compose -Arguments @('down','--volumes','--timeout','10') | Out-Null
    }
    foreach ($name in $previous.Keys) { [Environment]::SetEnvironmentVariable($name,$previous[$name],'Process') }
    foreach ($path in @($overlayPath,$envPath)) {
        if (Test-Path -LiteralPath $path) { Remove-Item -LiteralPath $path }
    }
}
