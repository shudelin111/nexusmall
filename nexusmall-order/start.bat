@echo off
REM =====================================================
REM NexusMall Order Service - Docker 快速启动脚本 (Windows PowerShell)
REM =====================================================
REM 用途：一键启动/停止开发环境
REM =====================================================

setlocal enabledelayedexpansion

REM 颜色定义 (ANSI)
set "BLUE=[0;34m"
set "GREEN=[0;32m"
set "YELLOW=[1;33m"
set "RED=[0;31m"
set "NC=[0m"

REM 脚本目录
cd /d "%~dp0"

echo.
echo =====================================================
echo   NexusMall Order Service - Docker 启动向导
echo =====================================================
echo.

REM 检查 Docker
docker --version >nul 2>&1
if errorlevel 1 (
    echo %RED%[ERROR]%NC% Docker 未安装，请先安装 Docker Desktop
    pause
    exit /b 1
)

echo %GREEN%[SUCCESS]%NC% Docker 环境检查通过
echo.

REM 创建目录
echo %BLUE%[INFO]%NC% 创建必要目录...
if not exist logs mkdir logs
if not exist config mkdir config
if not exist init-db mkdir init-db
if not exist rocketmq mkdir rocketmq
echo %GREEN%[SUCCESS]%NC% 目录创建完成
echo.

REM 检查初始化脚本
if not exist init-db\init.sql (
    echo %YELLOW%[WARNING]%NC% 未找到数据库初始化脚本，创建示例脚本...
    (
        echo -- NexusMall Order Database Initialization
        echo CREATE DATABASE IF NOT EXISTS `nexusmall_order` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
        echo USE `nexusmall_order`;
        echo.
        echo -- Create undo_log table for Seata
        echo CREATE TABLE IF NOT EXISTS `undo_log` ^(
        echo   `id` bigint^(20^) NOT NULL AUTO_INCREMENT,
        echo   `branch_id` bigint^(20^) NOT NULL,
        echo   `xid` varchar^(100^) NOT NULL,
        echo   `context` varchar^(128^) NOT NULL,
        echo   `rollback_info` longblob NOT NULL,
        echo   `log_status` int^(11^) NOT NULL,
        echo   `log_created` datetime NOT NULL,
        echo   `log_modified` datetime NOT NULL,
        echo   PRIMARY KEY ^(`id`^),
        echo   UNIQUE KEY `ux_undo_log` ^(`xid`,`branch_id`^)
        echo ^) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;
    ) > init-db\init.sql
    echo %GREEN%[SUCCESS]%NC% 数据库初始化脚本已创建
) else (
    echo %GREEN%[SUCCESS]%NC% 数据库初始化脚本已存在
)
echo.

REM 启动
echo %BLUE%[INFO]%NC% 启动所有服务...
docker-compose up -d

echo.
echo %BLUE%[INFO]%NC% 等待服务启动 ^(约 60 秒^)...
timeout /t 60 /nobreak >nul

echo.
echo %BLUE%[INFO]%NC% 检查服务状态...
docker-compose ps

echo.
echo %GREEN%====== 服务启动完成 ======%NC%
echo.
echo Order 服务：     http://localhost:11000
echo 健康检查：       http://localhost:11000/actuator/health
echo Prometheus:      http://localhost:11000/actuator/prometheus
echo MySQL:          localhost:3306 ^(root/root123456^)
echo Redis:          localhost:6379 ^(密码：redis123456^)
echo Nacos:          http://localhost:8848/nacos ^(nacos/nacos^)
echo Sentinel:       http://localhost:8858 ^(sentinel/sentinel^)
echo RocketMQ Dashboard: http://localhost:8080
echo.
echo %BLUE%[INFO]%NC% 查看日志：docker-compose logs -f nexusmall-order
echo %BLUE%[INFO]%NC% 停止服务：docker-compose down
echo.

pause
