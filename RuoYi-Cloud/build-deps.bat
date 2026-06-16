@echo off
chcp 65001 >nul
title ====== 首次构建依赖 ======

echo.
echo ========================================
echo   首次构建：安装公共依赖到本地仓库
echo   之后即可用 start-*.bat 直接启动
echo ========================================
echo.

set CLOUD_ROOT=%~dp0

echo [1/3] 清理旧构建缓存...
call mvn clean -q
echo.

echo [2/3] 编译并安装公共模块 (ruoyi-common + ruoyi-api) ...
call mvn install -DskipTests -pl ruoyi-common,ruoyi-api -am
if %ERRORLEVEL% neq 0 (
    echo [错误] 公共模块安装失败!
    pause
    exit /b 1
)
echo.

echo [3/3] 编译所有业务模块...
call mvn compile -DskipTests -pl ruoyi-modules -am
if %ERRORLEVEL% neq 0 (
    echo [错误] 业务模块编译失败!
    pause
    exit /b 1
)

echo.
echo ========================================
echo   构建完成! 现在可以用 start-all.bat 启动
echo ========================================
pause
