param(
    [string]$RuntimeImage = 'delivery-backend:mvp',
    [string]$JarPath,
    [string]$Docker = 'docker'
)
$ErrorActionPreference = 'Stop'
$suffix = [guid]::NewGuid().ToString('N').Substring(0,12)
$network = "delivery-smoke-$suffix"
$databaseContainer = "delivery-smoke-db-$suffix"
$applicationContainer = "delivery-smoke-app-$suffix"
$password = [guid]::NewGuid().ToString('N')
$jwtSecret = [guid]::NewGuid().ToString('N') + [guid]::NewGuid().ToString('N')

function Invoke-Docker {
    param([string[]]$Arguments)
    $output = & $Docker @Arguments
    if ($LASTEXITCODE -ne 0) { throw "Docker operation failed: $($Arguments[0])" }
    return $output
}
function Expect-Status {
    param($Response, [int]$Expected, [string]$Step)
    if ([int]$Response.StatusCode -ne $Expected) { throw "$Step expected $Expected, got $($Response.StatusCode)" }
}

try {
    Invoke-Docker -Arguments @('network','create',$network) | Out-Null
    Invoke-Docker -Arguments @('run','-d','--name',$databaseContainer,'--network',$network,'--network-alias','postgres',
        '--tmpfs','/var/lib/postgresql/data','-e','POSTGRES_DB=delivery_smoke','-e','POSTGRES_USER=delivery',
        '-e',"POSTGRES_PASSWORD=$password",'postgres:17-alpine') | Out-Null
    $ready = $false
    for ($attempt = 0; $attempt -lt 40; $attempt++) {
        & $Docker exec $databaseContainer pg_isready -U delivery -d delivery_smoke 2>$null | Out-Null
        if ($LASTEXITCODE -eq 0) { $ready = $true; break }
        Start-Sleep -Seconds 1
    }
    if (-not $ready) { throw 'Isolated PostgreSQL did not become ready' }
    $arguments = @('run','-d','--name',$applicationContainer,'--network',$network,'-p','127.0.0.1::8080',
        '-e','DB_HOST=postgres','-e','DB_PORT=5432','-e','DB_NAME=delivery_smoke','-e','DB_USER=delivery',
        '-e',"DB_PASSWORD=$password",'-e',"JWT_SECRET=$jwtSecret",'-e','PORT=8080','-e','HOST=0.0.0.0')
    if ($JarPath) {
        $resolvedJar = (Resolve-Path -LiteralPath $JarPath).Path
        $arguments += @('--mount',"type=bind,source=$resolvedJar,target=/verification/app.jar,readonly",'--entrypoint','java')
    }
    $arguments += $RuntimeImage
    if ($JarPath) { $arguments += @('-jar','/verification/app.jar') }
    Invoke-Docker -Arguments $arguments | Out-Null
    $binding = (Invoke-Docker -Arguments @('port',$applicationContainer,'8080/tcp')).Trim()
    if ($binding -notmatch '^127\.0\.0\.1:(\d+)$') { throw 'Unexpected smoke-test port binding' }
    $baseUrl = "http://127.0.0.1:$($Matches[1])"
    $ready = $false
    for ($attempt = 0; $attempt -lt 60; $attempt++) {
        $state = (Invoke-Docker -Arguments @('inspect','--format','{{.State.Running}}',$applicationContainer)).Trim()
        if ($state -ne 'true') { break }
        try {
            $probe = Invoke-WebRequest "$baseUrl/ready" -SkipHttpErrorCheck -TimeoutSec 3
            if ($probe.StatusCode -eq 200) { $ready = $true; break }
        } catch { }
        Start-Sleep -Seconds 1
    }
    if (-not $ready) {
        & $Docker logs --tail 60 $applicationContainer
        throw 'Packaged application did not become ready'
    }
    Expect-Status (Invoke-WebRequest "$baseUrl/live") 200 'Liveness'
    $contract = Invoke-RestMethod "$baseUrl/openapi.json"
    if ($contract.openapi -ne '3.0.3') { throw 'OpenAPI document unavailable' }
    Expect-Status (Invoke-WebRequest "$baseUrl/swagger") 200 'Swagger'
    $registration = @{email='smoke@example.com';phone='+5355550100';password='SmokePassword123!';firstName='Smoke';lastName='Test'} | ConvertTo-Json
    $registered = Invoke-WebRequest "$baseUrl/auth/register" -Method Post -ContentType 'application/json' -Body $registration -SkipHttpErrorCheck
    Expect-Status $registered 201 'Registration'
    $session = $registered.Content | ConvertFrom-Json
    if ($session.roles -notcontains 'CUSTOMER') { throw 'Registration did not produce CUSTOMER' }
    $me = Invoke-WebRequest "$baseUrl/auth/me" -Headers @{Authorization="Bearer $($session.accessToken)"}
    Expect-Status $me 200 'Current user'
    if (($me.Content | ConvertFrom-Json).id -ne $session.userId) { throw 'Wrong authenticated user' }
    $loginBody = @{identifier='smoke@example.com';password='SmokePassword123!'} | ConvertTo-Json
    $login = Invoke-WebRequest "$baseUrl/auth/login" -Method Post -ContentType 'application/json' -Body $loginBody -SkipHttpErrorCheck
    Expect-Status $login 200 'Login'
    $session = $login.Content | ConvertFrom-Json
    $refreshBody = @{refreshToken=$session.refreshToken} | ConvertTo-Json
    $rotated = Invoke-WebRequest "$baseUrl/auth/refresh" -Method Post -ContentType 'application/json' -Body $refreshBody -SkipHttpErrorCheck
    Expect-Status $rotated 200 'Refresh'
    Expect-Status (Invoke-WebRequest "$baseUrl/auth/refresh" -Method Post -ContentType 'application/json' -Body $refreshBody -SkipHttpErrorCheck) 401 'Refresh replay'
    $replacement = $rotated.Content | ConvertFrom-Json
    $logoutBody = @{refreshToken=$replacement.refreshToken} | ConvertTo-Json
    Expect-Status (Invoke-WebRequest "$baseUrl/auth/logout" -Method Post -ContentType 'application/json' -Body $logoutBody -SkipHttpErrorCheck) 204 'Logout'
    Expect-Status (Invoke-WebRequest "$baseUrl/auth/refresh" -Method Post -ContentType 'application/json' -Body $logoutBody -SkipHttpErrorCheck) 401 'Revoked token'
    $migrations = (Invoke-Docker -Arguments @('exec',$databaseContainer,'psql','-U','delivery','-d','delivery_smoke','-Atc',
        'SELECT count(*) FROM flyway_schema_history WHERE success')).Trim()
    if ($migrations -ne '6') { throw "Expected 6 successful migrations, got $migrations" }
    Invoke-Docker -Arguments @('stop',$databaseContainer) | Out-Null
    Expect-Status (Invoke-WebRequest "$baseUrl/ready" -SkipHttpErrorCheck -TimeoutSec 20) 503 'Database outage'
    Expect-Status (Invoke-WebRequest "$baseUrl/live") 200 'Liveness during outage'
    Write-Output 'SMOKE PASSED: isolated PostgreSQL, V1-V6, packaged Ktor, auth lifecycle, OpenAPI, Swagger, readiness and liveness.'
} finally {
    # These names are generated for this invocation; no development containers or volumes are touched.
    & $Docker rm -f $applicationContainer $databaseContainer 2>$null | Out-Null
    & $Docker network rm $network 2>$null | Out-Null
}
