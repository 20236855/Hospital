@echo off
setlocal EnableExtensions

echo.
echo [INFO] Stopping hospital backend services by port...

call :killPort 8080 gateway
call :killPort 9200 auth
call :killPort 9201 system
call :killPort 9300 file
call :killPort 9210 patient
call :killPort 9211 doctor
call :killPort 9212 register
call :killPort 9213 emr
call :killPort 9214 prescription
call :killPort 9215 exam
call :killPort 9216 payment

echo [INFO] Stop finished.
if /I not "%~1"=="nopause" pause
exit /b 0

:killPort
set "PORT=%~1"
set "NAME=%~2"
set "FOUND="

for /f "tokens=5" %%P in ('netstat -ano ^| findstr /R /C:":%PORT% .*LISTENING"') do (
    set "FOUND=1"
    echo [INFO] Killing %NAME% port %PORT% pid %%P
    taskkill /F /PID %%P >nul 2>&1
)

if not defined FOUND echo [INFO] %NAME% port %PORT% is not listening.
goto :eof
