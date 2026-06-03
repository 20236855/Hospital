# RuoYi-Cloud-Vue3 微服务项目运行文档

## 1. 项目目录

当前项目放在：

```text
C:\Users\Admin\Desktop\实训\hosipital\code
```

目录说明：

```text
code
├─ RuoYi-Cloud        后端微服务项目，当前使用 master 分支
└─ RuoYi-Cloud-Vue3   Vue3 前端项目
```

辅助工具和日志目录：

```text
C:\Users\Admin\Desktop\实训\hosipital\tools
├─ jdk17\jdk-17.0.19+10    免安装 JDK17
├─ nacos3\nacos            Nacos 3.0.2
└─ logs                    前后端启动日志
```

## 2. 当前技术栈版本

### 2.1 后端技术栈

| 技术 | 当前版本 |
| --- | --- |
| RuoYi | 3.6.8 |
| JDK | Eclipse Temurin 17.0.19 |
| Maven | 3.6.3 |
| Spring Boot | 4.0.3 |
| Spring Cloud | 2025.1.0 |
| Spring Cloud Alibaba | 2025.1.0.0 |
| Spring Boot Admin | 4.0.2 |
| MyBatis Spring Boot | 4.0.1 |
| MySQL | 8.0.41 |
| Redis | 5.0.14.1 |
| Nacos | 3.0.2 |

注意：系统默认 `java -version` 是 JDK8，但本项目后端实际运行使用的是：

```text
C:\Users\Admin\Desktop\实训\hosipital\tools\jdk17\jdk-17.0.19+10
```

所以启动或编译后端时必须临时设置 `JAVA_HOME` 为上面的 JDK17。

### 2.2 前端技术栈

| 技术 | 当前版本 |
| --- | --- |
| Node.js | 22.14.0 |
| npm | 10.9.2 |
| Vue | 3.5.26 |
| Vite | 6.4.1 |
| Element Plus | 2.13.1 |
| Pinia | 3.0.4 |
| Vue Router | 4.6.4 |
| Axios | 1.13.2 |
| ECharts | 5.6.0 |

## 3. 当前端口分配

| 服务 | 端口 | 说明 |
| --- | ---: | --- |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| Nacos Server | 8848 | 后端服务注册与配置中心 |
| Nacos gRPC | 9848、9849 | Nacos 内部通信 |
| Nacos Console | 18080 | Nacos 控制台，已避开网关 8080 |
| RuoYi Gateway | 8080 | 后端网关 |
| RuoYi Auth | 9200 | 认证服务 |
| RuoYi System | 9201 | 系统服务 |
| RuoYi Gen | 9202 | 代码生成服务 |
| RuoYi Job | 9203 | 定时任务服务 |
| RuoYi File | 9300 | 文件服务 |
| Vue3 前端 | 8081 | 前端开发服务 |

## 4. 数据库信息

MySQL 账号：

```text
用户名：root
密码：root
```

当前已初始化数据库：

```text
ry-cloud
ry-config
ry-seata
```

如果需要重新初始化数据库，执行：

```powershell
& 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe' -uroot -proot -e 'CREATE DATABASE IF NOT EXISTS `ry-cloud` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;'

& 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe' -uroot -proot ry-cloud --default-character-set=utf8mb4 -e 'source C:/Users/Admin/Desktop/实训/hosipital/code/RuoYi-Cloud/sql/ry_20260417.sql; source C:/Users/Admin/Desktop/实训/hosipital/code/RuoYi-Cloud/sql/quartz.sql;'

& 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe' -uroot -proot --default-character-set=utf8mb4 -e 'source C:/Users/Admin/Desktop/实训/hosipital/code/RuoYi-Cloud/sql/ry_config_20260311.sql; source C:/Users/Admin/Desktop/实训/hosipital/code/RuoYi-Cloud/sql/ry_seata_20210128.sql;'
```

如果重新导入了 `ry_config_20260311.sql`，需要把 Nacos 配置库里的数据库密码改成 `root`：

```powershell
& 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe' -uroot -proot --default-character-set=utf8mb4 -e "UPDATE `ry-config`.config_info SET content = REPLACE(content, 'password: password', 'password: root');"
```

## 5. 启动顺序

必须按下面顺序启动：

```text
1. MySQL
2. Redis
3. Nacos
4. 后端微服务
5. 前端 Vue3
```

## 6. 启动 MySQL

MySQL 已安装为 Windows 服务，服务名是 `MySQL80`。

启动：

```powershell
Start-Service MySQL80
```

验证：

```powershell
& 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe' -uroot -proot -e "select version();"
```

## 7. 启动 Redis

启动：

```powershell
Start-Process -FilePath 'C:\Users\Admin\Downloads\Redis-x64-5.0.14.1(1)\redis-server.exe' -ArgumentList @('--port','6379') -WorkingDirectory 'C:\Users\Admin\Downloads\Redis-x64-5.0.14.1(1)' -WindowStyle Hidden
```

验证端口：

```powershell
Get-NetTCPConnection -LocalPort 6379 -ErrorAction SilentlyContinue
```

## 8. 启动 Nacos

Nacos 使用 3.0.2，服务端口是 `8848`，控制台端口改成了 `18080`，避免和若依网关 `8080` 冲突。

启动：

```powershell
Start-Process -FilePath 'cmd.exe' -ArgumentList '/c "set JAVA_HOME=C:\Users\Admin\Desktop\实训\hosipital\tools\jdk17\jdk-17.0.19+10&& set PATH=C:\Users\Admin\Desktop\实训\hosipital\tools\jdk17\jdk-17.0.19+10\bin;%PATH%&& C:\Users\Admin\Desktop\实训\hosipital\tools\nacos3\nacos\bin\startup.cmd -m standalone"' -WorkingDirectory 'C:\Users\Admin\Desktop\实训\hosipital\tools\nacos3\nacos\bin' -WindowStyle Hidden
```

验证端口：

```powershell
Get-NetTCPConnection -LocalPort 8848,9848,9849,18080 -ErrorAction SilentlyContinue
```

访问地址：

```text
Nacos 服务端：http://localhost:8848/nacos
Nacos 控制台：http://localhost:18080
```

## 9. 编译后端

如果已经编译过，并且代码没有变，可以跳过本节。

进入后端目录：

```powershell
cd C:\Users\Admin\Desktop\实训\hosipital\code\RuoYi-Cloud
```

使用 JDK17 编译：

```powershell
$env:JAVA_HOME='C:\Users\Admin\Desktop\实训\hosipital\tools\jdk17\jdk-17.0.19+10'
$env:Path="$env:JAVA_HOME\bin;C:\Program Files\JetBrains\IntelliJ IDEA 2021.2\plugins\maven\lib\maven3\bin;$env:Path"
& 'C:\Program Files\JetBrains\IntelliJ IDEA 2021.2\plugins\maven\lib\maven3\bin\mvn.cmd' clean install -DskipTests
```

编译完成后会生成这些核心 jar：

```text
RuoYi-Cloud\ruoyi-gateway\target\ruoyi-gateway.jar
RuoYi-Cloud\ruoyi-auth\target\ruoyi-auth.jar
RuoYi-Cloud\ruoyi-modules\ruoyi-system\target\ruoyi-modules-system.jar
RuoYi-Cloud\ruoyi-modules\ruoyi-gen\target\ruoyi-modules-gen.jar
RuoYi-Cloud\ruoyi-modules\ruoyi-job\target\ruoyi-modules-job.jar
RuoYi-Cloud\ruoyi-modules\ruoyi-file\target\ruoyi-modules-file.jar
```

## 10. 启动后端微服务

推荐直接运行脚本：

```powershell
powershell -ExecutionPolicy Bypass -File C:\Users\Admin\Desktop\实训\hosipital\code\start-ruoyi-backend.ps1
```

说明：本机可能存在多个网卡，若不指定注册 IP，微服务可能注册成 `10.x.x.x` 地址，导致网关转发到 `10.x.x.x:9201` 超时。脚本已强制指定：

```text
spring.cloud.nacos.discovery.ip=127.0.0.1
spring.cloud.client.ip-address=127.0.0.1
```

启动命令：

```powershell
$java='C:\Users\Admin\Desktop\实训\hosipital\tools\jdk17\jdk-17.0.19+10\bin\java.exe'
$base='C:\Users\Admin\Desktop\实训\hosipital\code\RuoYi-Cloud'
$log='C:\Users\Admin\Desktop\实训\hosipital\tools\logs'
New-Item -ItemType Directory -Path $log -Force | Out-Null
$common=@('-Dspring.cloud.nacos.discovery.ip=127.0.0.1','-Dspring.cloud.client.ip-address=127.0.0.1')

$services=@(
  @('gateway',"$base\ruoyi-gateway\target",'ruoyi-gateway.jar'),
  @('auth',"$base\ruoyi-auth\target",'ruoyi-auth.jar'),
  @('system',"$base\ruoyi-modules\ruoyi-system\target",'ruoyi-modules-system.jar'),
  @('gen',"$base\ruoyi-modules\ruoyi-gen\target",'ruoyi-modules-gen.jar'),
  @('job',"$base\ruoyi-modules\ruoyi-job\target",'ruoyi-modules-job.jar'),
  @('file',"$base\ruoyi-modules\ruoyi-file\target",'ruoyi-modules-file.jar')
)

foreach($s in $services){
  Start-Process -FilePath $java -ArgumentList @($common[0],$common[1],'-jar',$s[2]) -WorkingDirectory $s[1] -WindowStyle Hidden -RedirectStandardOutput "$log\$($s[0]).out.log" -RedirectStandardError "$log\$($s[0]).err.log"
  Start-Sleep -Seconds 2
}
```

验证端口：

```powershell
Get-NetTCPConnection -LocalPort 8080,9200,9201,9202,9203,9300 -ErrorAction SilentlyContinue
```

验证验证码接口：

```powershell
Invoke-WebRequest -Uri 'http://localhost:8080/code' -UseBasicParsing
```

## 11. 启动前端

前端目录：

```powershell
cd C:\Users\Admin\Desktop\实训\hosipital\code\RuoYi-Cloud-Vue3
```

如果没有安装依赖，先执行：

```powershell
npm.cmd install --registry=https://registry.npmmirror.com
```

启动前端：

```powershell
Start-Process -FilePath 'npm.cmd' -ArgumentList @('run','dev','--','--host','0.0.0.0','--port','8081') -WorkingDirectory 'C:\Users\Admin\Desktop\实训\hosipital\code\RuoYi-Cloud-Vue3' -WindowStyle Hidden -RedirectStandardOutput 'C:\Users\Admin\Desktop\实训\hosipital\tools\logs\frontend.out.log' -RedirectStandardError 'C:\Users\Admin\Desktop\实训\hosipital\tools\logs\frontend.err.log'
```

访问地址：

```text
http://localhost:8081/
```

默认账号：

```text
admin / admin123
```

验证前端页面：

```powershell
Invoke-WebRequest -Uri 'http://localhost:8081/' -UseBasicParsing
```

验证前端代理到后端：

```powershell
Invoke-WebRequest -Uri 'http://localhost:8081/dev-api/code' -UseBasicParsing
```

## 12. 一键查看当前运行状态

查看端口：

```powershell
Get-NetTCPConnection -LocalPort 3306,6379,8848,9848,9849,18080,8080,9200,9201,9202,9203,9300,8081 -ErrorAction SilentlyContinue | Select-Object LocalPort,State,OwningProcess | Sort-Object LocalPort
```

查看 Java 后端进程：

```powershell
Get-CimInstance Win32_Process -Filter "name = 'java.exe'" | Select-Object ProcessId,CommandLine
```

查看前端进程：

```powershell
Get-CimInstance Win32_Process | Where-Object { $_.CommandLine -match 'vite|RuoYi-Cloud-Vue3' } | Select-Object ProcessId,Name,CommandLine
```

查看 Redis 进程：

```powershell
Get-CimInstance Win32_Process | Where-Object { $_.CommandLine -match 'redis-server' } | Select-Object ProcessId,Name,CommandLine
```

## 13. 停止前端

停止 Vue3/Vite 前端：

```powershell
Get-CimInstance Win32_Process | Where-Object { $_.CommandLine -match 'RuoYi-Cloud-Vue3|vite --host 0.0.0.0 --port 8081' } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force }
```

确认端口释放：

```powershell
Get-NetTCPConnection -LocalPort 8081 -ErrorAction SilentlyContinue
```

## 14. 停止后端微服务

停止若依后端 jar 服务：

```powershell
Get-CimInstance Win32_Process -Filter "name = 'java.exe'" |
  Where-Object {
    $_.CommandLine -match 'ruoyi-gateway.jar|ruoyi-auth.jar|ruoyi-modules-system.jar|ruoyi-modules-gen.jar|ruoyi-modules-job.jar|ruoyi-modules-file.jar'
  } |
  ForEach-Object { Stop-Process -Id $_.ProcessId -Force }
```

确认端口释放：

```powershell
Get-NetTCPConnection -LocalPort 8080,9200,9201,9202,9203,9300 -ErrorAction SilentlyContinue
```

## 15. 停止 Nacos

停止 Nacos：

```powershell
Get-CimInstance Win32_Process -Filter "name = 'java.exe'" |
  Where-Object { $_.CommandLine -match 'nacos' } |
  ForEach-Object { Stop-Process -Id $_.ProcessId -Force }
```

确认端口释放：

```powershell
Get-NetTCPConnection -LocalPort 8848,9848,9849,18080 -ErrorAction SilentlyContinue
```

## 16. 停止 Redis

停止 Redis：

```powershell
Get-CimInstance Win32_Process | Where-Object { $_.CommandLine -match 'redis-server' } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force }
```

确认端口释放：

```powershell
Get-NetTCPConnection -LocalPort 6379 -ErrorAction SilentlyContinue
```

## 17. 停止 MySQL 服务

停止 MySQL：

```powershell
Stop-Service MySQL80
```

如果普通 PowerShell 权限不足，请用管理员身份打开 PowerShell 后执行：

```powershell
net stop MySQL80
```

确认状态：

```powershell
Get-Service MySQL80
```

## 18. 推荐停止顺序

停止时建议按下面顺序：

```text
1. 前端
2. 后端微服务
3. Nacos
4. Redis
5. MySQL
```

## 19. 推荐启动顺序

重启电脑后建议按下面顺序：

```text
1. 启动 MySQL
2. 启动 Redis
3. 启动 Nacos
4. 启动后端微服务
5. 启动前端
```

## 20. 日志位置

前后端启动日志：

```text
C:\Users\Admin\Desktop\实训\hosipital\tools\logs
```

Nacos 日志：

```text
C:\Users\Admin\Desktop\实训\hosipital\tools\nacos3\nacos\logs
```

常用查看命令：

```powershell
Get-Content -LiteralPath 'C:\Users\Admin\Desktop\实训\hosipital\tools\logs\frontend.out.log' -Tail 80
Get-Content -LiteralPath 'C:\Users\Admin\Desktop\实训\hosipital\tools\nacos3\nacos\logs\nacos.log' -Tail 120
```

## 21. 常见问题

### 21.1 npm run dev 提示 vite is not recognized

原因：前端依赖没有安装。

解决：

```powershell
cd C:\Users\Admin\Desktop\实训\hosipital\code\RuoYi-Cloud-Vue3
npm.cmd install --registry=https://registry.npmmirror.com
npm.cmd run dev -- --host 0.0.0.0 --port 8081
```

### 21.2 PowerShell 提示 npm.ps1 禁止运行

原因：PowerShell 执行策略限制脚本。

解决：使用 `npm.cmd`，不要直接使用 `npm`。

```powershell
npm.cmd install
npm.cmd run dev
```

### 21.3 8080 端口被占用

若依网关必须使用 `8080`。本机 Nacos 3 默认控制台也会占用 `8080`，因此已经把 Nacos 控制台改成 `18080`。

检查 8080：

```powershell
Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue
```

### 21.4 后端启动失败，提示连接数据库失败

检查 MySQL 是否运行：

```powershell
Get-Service MySQL80
```

检查账号密码：

```powershell
& 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe' -uroot -proot -e "select version();"
```

检查 Nacos 配置库中的数据库密码是否为 `root`。

### 21.5 后端启动失败，提示 Nacos 连接失败

检查 Nacos：

```powershell
Get-NetTCPConnection -LocalPort 8848,9848,9849 -ErrorAction SilentlyContinue
```

查看 Nacos 日志：

```powershell
Get-Content -LiteralPath 'C:\Users\Admin\Desktop\实训\hosipital\tools\nacos3\nacos\logs\nacos.log' -Tail 120
```

### 21.6 Maven 编译时使用了 JDK8

本机系统默认 Java 是 JDK8，所以编译前必须执行：

```powershell
$env:JAVA_HOME='C:\Users\Admin\Desktop\实训\hosipital\tools\jdk17\jdk-17.0.19+10'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
java -version
```

确认输出是 `17.0.19` 后再执行 Maven 编译。

## 22. 当前可访问地址

```text
前端：http://localhost:8081/
后端网关：http://localhost:8080
验证码接口：http://localhost:8080/code
前端代理验证码接口：http://localhost:8081/dev-api/code
Nacos 服务端：http://localhost:8848/nacos
Nacos 控制台：http://localhost:18080
```
