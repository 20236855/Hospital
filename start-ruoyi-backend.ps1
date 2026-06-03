$java = 'C:\Users\Admin\Desktop\实训\hosipital\tools\jdk17\jdk-17.0.19+10\bin\java.exe'
$base = 'C:\Users\Admin\Desktop\实训\hosipital\code\RuoYi-Cloud'
$log = 'C:\Users\Admin\Desktop\实训\hosipital\tools\logs'

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
