@echo off
setlocal EnableExtensions

set "JAVA_HOME=D:\Tool\jdk-17\jdk-17"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "BASE_DIR=%~dp0.."
set "JAR_DIR=D:\Hospital-Backend-Jars"
set "LOG_DIR=D:\Hospital-Backend-Logs"
set "MAVEN_REPO=D:\Hospital-Maven-Repo"
set "JAVA_OPTS=-Xms256m -Xmx768m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m"

if not exist "%JAR_DIR%" mkdir "%JAR_DIR%"
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
if not exist "%MAVEN_REPO%" mkdir "%MAVEN_REPO%"

echo.
echo [INFO] Hospital backend starter
echo [INFO] JAVA_HOME=%JAVA_HOME%
echo [INFO] Maven repo: %MAVEN_REPO%
echo [INFO] Jar dir: %JAR_DIR%
echo [INFO] Make sure Nacos is running first:
echo [INFO]   set "JAVA_HOME=D:\Tool\jdk-17\jdk-17" ^&^& set "PATH=%%JAVA_HOME%%\bin;%%PATH%%" ^&^& startup.cmd -m standalone
echo.

call "%~dp0stop-hospital-backend.cmd" nopause

if /I "%~1"=="nobuild" goto START_SERVICES

echo.
echo [INFO] Packaging backend jars...
pushd "%BASE_DIR%"
call mvn -Dmaven.repo.local="%MAVEN_REPO%" -pl ruoyi-gateway,ruoyi-auth,ruoyi-modules/ruoyi-system,ruoyi-modules/ruoyi-file,ruoyi-modules/his-patient,ruoyi-modules/his-doctor,ruoyi-modules/his-payment,ruoyi-modules/his-emr,ruoyi-modules/his-register,ruoyi-modules/his-exam,ruoyi-modules/his-prescription -am package -DskipTests
if errorlevel 1 (
    popd
    echo.
    echo [ERROR] Maven package failed. Services were not started.
    pause
    exit /b 1
)
popd

echo.
echo [INFO] Copying jars to %JAR_DIR%...
set "COPY_FAILED="
call :copyJar "%BASE_DIR%\ruoyi-gateway\target\ruoyi-gateway.jar"                  "ruoyi-gateway.jar"
call :copyJar "%BASE_DIR%\ruoyi-auth\target\ruoyi-auth.jar"                        "ruoyi-auth.jar"
call :copyJar "%BASE_DIR%\ruoyi-modules\ruoyi-system\target\ruoyi-modules-system.jar" "ruoyi-modules-system.jar"
call :copyJar "%BASE_DIR%\ruoyi-modules\ruoyi-file\target\ruoyi-modules-file.jar"  "ruoyi-modules-file.jar"
call :copyJar "%BASE_DIR%\ruoyi-modules\his-patient\target\his-patient.jar"        "his-patient.jar"
call :copyJar "%BASE_DIR%\ruoyi-modules\his-doctor\target\his-doctor.jar"          "his-doctor.jar"
call :copyJar "%BASE_DIR%\ruoyi-modules\his-register\target\his-register.jar"      "his-register.jar"
call :copyJar "%BASE_DIR%\ruoyi-modules\his-emr\target\his-emr.jar"                "his-emr.jar"
call :copyJar "%BASE_DIR%\ruoyi-modules\his-prescription\target\his-prescription.jar" "his-prescription.jar"
call :copyJar "%BASE_DIR%\ruoyi-modules\his-exam\target\his-exam.jar"              "his-exam.jar"
call :copyJar "%BASE_DIR%\ruoyi-modules\his-payment\target\his-payment.jar"        "his-payment.jar"
if defined COPY_FAILED (
    echo.
    echo [ERROR] One or more jars failed to copy. Services were not started.
    pause
    exit /b 1
)

:START_SERVICES
echo.
echo [INFO] Starting services. Logs: %LOG_DIR%

call :startService "gateway"      "ruoyi-gateway.jar"          8080
call :startService "auth"         "ruoyi-auth.jar"             9200
call :startService "system"       "ruoyi-modules-system.jar"   9201
call :startService "file"         "ruoyi-modules-file.jar"     9300
call :startService "patient"      "his-patient.jar"            9210
call :startService "doctor"       "his-doctor.jar"             9211
call :startService "register"     "his-register.jar"           9212
call :startService "emr"          "his-emr.jar"                9213
call :startService "prescription" "his-prescription.jar"       9214
call :startService "exam"         "his-exam.jar"               9215
call :startService "payment"      "his-payment.jar"            9216

echo.
echo [INFO] Start commands have been issued.
echo [INFO] Check logs in: %LOG_DIR%
echo [INFO] Started jars from: %JAR_DIR%
echo [INFO] To skip packaging next time: start-hospital-backend.cmd nobuild
pause
exit /b 0

:copyJar
set "SRC_JAR=%~1"
set "DST_JAR=%~2"
if not exist "%SRC_JAR%" (
    echo [WARN] Jar not found, skip copy: %SRC_JAR%
    goto :eof
)
copy /Y "%SRC_JAR%" "%JAR_DIR%\%DST_JAR%" >nul
if errorlevel 1 (
    echo [ERROR] Failed to copy %SRC_JAR% to %JAR_DIR%\%DST_JAR%
    set "COPY_FAILED=1"
    goto :eof
)
echo [INFO] Copied %DST_JAR%
goto :eof

:startService
set "SVC_NAME=%~1"
set "SVC_JAR=%~2"
set "SVC_PORT=%~3"

if not exist "%JAR_DIR%\%SVC_JAR%" (
    echo [WARN] %SVC_NAME% jar not found: %JAR_DIR%\%SVC_JAR%
    echo [WARN] Skip %SVC_NAME%.
    goto :eof
)

echo [INFO] Starting %SVC_NAME% on port %SVC_PORT%...
start "%SVC_NAME%-%SVC_PORT%" /min /D "%JAR_DIR%" cmd /c "java -Dfile.encoding=utf-8 %JAVA_OPTS% -jar %SVC_JAR% > %LOG_DIR%\%SVC_NAME%.log 2>&1"
timeout /t 1 /nobreak >nul
goto :eof
