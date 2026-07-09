$repoRoot = $PSScriptRoot
$base = Join-Path $repoRoot 'RuoYi-Cloud'
$log = Join-Path $repoRoot 'run-logs\backend'
$preferredJavaHome = 'D:\Tool\jdk-17\jdk-17'
$java = if (Test-Path (Join-Path $preferredJavaHome 'bin\java.exe')) {
  Join-Path $preferredJavaHome 'bin\java.exe'
} elseif ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME 'bin\java.exe'))) {
  Join-Path $env:JAVA_HOME 'bin\java.exe'
} else {
  'java.exe'
}

New-Item -ItemType Directory -Path $log -Force | Out-Null

$services = @(
  @('gateway', "$base\ruoyi-gateway\target", 'ruoyi-gateway.jar'),
  @('auth', "$base\ruoyi-auth\target", 'ruoyi-auth.jar'),
  @('system', "$base\ruoyi-modules\ruoyi-system\target", 'ruoyi-modules-system.jar'),
  @('gen', "$base\ruoyi-modules\ruoyi-gen\target", 'ruoyi-modules-gen.jar'),
  @('job', "$base\ruoyi-modules\ruoyi-job\target", 'ruoyi-modules-job.jar'),
  @('file', "$base\ruoyi-modules\ruoyi-file\target", 'ruoyi-modules-file.jar'),
  @('his-doctor', "$base\ruoyi-modules\his-doctor\target", 'his-doctor.jar')
)

$commonArgs = @(
  '-Dspring.cloud.nacos.discovery.ip=127.0.0.1',
  '-Dspring.cloud.client.ip-address=127.0.0.1'
)

foreach ($service in $services) {
  $name = $service[0]
  $workDir = $service[1]
  $jar = $service[2]
  $jarPath = Join-Path $workDir $jar
  if (!(Test-Path -LiteralPath $jarPath)) {
    Write-Warning "Skip $name, jar not found: $jarPath"
    continue
  }
  $args = @($commonArgs[0], $commonArgs[1], '-jar', $jar)

  Start-Process -FilePath $java `
    -ArgumentList $args `
    -WorkingDirectory $workDir `
    -WindowStyle Hidden `
    -RedirectStandardOutput "$log\$name.out.log" `
    -RedirectStandardError "$log\$name.err.log"

  Start-Sleep -Seconds 2
}

Start-Sleep -Seconds 35
Get-NetTCPConnection -LocalPort 8080,9200,9201,9202,9203,9300 -ErrorAction SilentlyContinue |
  Select-Object LocalPort,State,OwningProcess |
  Sort-Object LocalPort
