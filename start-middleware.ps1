param(
  [switch]$StatusOnly
)

$ErrorActionPreference = 'Stop'

$mysqlService = 'MySQL80'
$redisHome = 'C:\Users\Admin\Downloads\Redis-x64-5.0.14.1(1)'
$redisExe = Join-Path $redisHome 'redis-server.exe'
$javaHome = 'C:\Users\Admin\Desktop\实训\hosipital\tools\jdk17\jdk-17.0.19+10'
$nacosBin = 'C:\Users\Admin\Desktop\实训\hosipital\tools\nacos3\nacos\bin'
$nacosStartup = Join-Path $nacosBin 'startup.cmd'
$logDir = 'C:\Users\Admin\Desktop\实训\hosipital\tools\logs'

New-Item -ItemType Directory -Path $logDir -Force | Out-Null

function Test-Port {
  param([int]$Port)

  $conn = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue |
    Where-Object { $_.State -eq 'Listen' -or $_.State -eq 'Established' }

  return $null -ne $conn
}

function Wait-Port {
  param(
    [int]$Port,
    [int]$TimeoutSeconds = 60
  )

  $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
  while ((Get-Date) -lt $deadline) {
    if (Test-Port -Port $Port) {
      return $true
    }
    Start-Sleep -Seconds 1
  }

  return $false
}

function Show-Status {
  Write-Host ''
  Write-Host 'Current middleware ports:'
  Get-NetTCPConnection -LocalPort 3306,6379,8848,9848,9849,18080 -ErrorAction SilentlyContinue |
    Select-Object LocalPort,State,OwningProcess |
    Sort-Object LocalPort |
    Format-Table -AutoSize

  Write-Host 'MySQL service:'
  Get-Service -Name $mysqlService -ErrorAction SilentlyContinue |
    Select-Object Name,Status |
    Format-Table -AutoSize
}

function Start-MySql {
  Write-Host '[1/3] MySQL'

  $service = Get-Service -Name $mysqlService -ErrorAction Stop
  if ($service.Status -ne 'Running') {
    Start-Service -Name $mysqlService
    $service.WaitForStatus('Running', '00:00:30')
  }

  if (Wait-Port -Port 3306 -TimeoutSeconds 30) {
    Write-Host 'MySQL is ready on port 3306.'
  } else {
    Write-Warning 'MySQL service was started, but port 3306 was not detected.'
  }
}

function Start-Redis {
  Write-Host '[2/3] Redis'

  if (Test-Port -Port 6379) {
    Write-Host 'Redis is already running on port 6379.'
    return
  }

  if (!(Test-Path -LiteralPath $redisExe)) {
    throw "Redis executable not found: $redisExe"
  }

  Start-Process `
    -FilePath $redisExe `
    -ArgumentList @('--port', '6379') `
    -WorkingDirectory $redisHome `
    -WindowStyle Hidden `
    -RedirectStandardOutput (Join-Path $logDir 'redis.out.log') `
    -RedirectStandardError (Join-Path $logDir 'redis.err.log')

  if (Wait-Port -Port 6379 -TimeoutSeconds 30) {
    Write-Host 'Redis is ready on port 6379.'
  } else {
    Write-Warning 'Redis was started, but port 6379 was not detected. Check redis.err.log.'
  }
}

function Start-Nacos {
  Write-Host '[3/3] Nacos'

  if (Test-Port -Port 8848) {
    Write-Host 'Nacos is already running on port 8848.'
    return
  }

  if (!(Test-Path -LiteralPath $nacosStartup)) {
    throw "Nacos startup script not found: $nacosStartup"
  }

  if (!(Test-Path -LiteralPath (Join-Path $javaHome 'bin\java.exe'))) {
    throw "JDK17 java.exe not found under: $javaHome"
  }

  $cmd = "set JAVA_HOME=$javaHome&& set PATH=$javaHome\bin;%PATH%&& `"$nacosStartup`" -m standalone"

  Start-Process `
    -FilePath 'cmd.exe' `
    -ArgumentList @('/c', $cmd) `
    -WorkingDirectory $nacosBin `
    -WindowStyle Hidden

  if (Wait-Port -Port 8848 -TimeoutSeconds 90) {
    Write-Host 'Nacos server is ready on port 8848.'
  } else {
    Write-Warning 'Nacos was started, but port 8848 was not detected. Check nacos logs.'
  }

  if (Wait-Port -Port 18080 -TimeoutSeconds 30) {
    Write-Host 'Nacos console is ready on port 18080.'
  } else {
    Write-Warning 'Nacos console port 18080 was not detected yet.'
  }
}

if ($StatusOnly) {
  Show-Status
  exit 0
}

Start-MySql
Start-Redis
Start-Nacos
Show-Status

Write-Host ''
Write-Host 'Middleware startup finished.'
Write-Host 'Nacos server:  http://localhost:8848/nacos'
Write-Host 'Nacos console: http://localhost:18080'
