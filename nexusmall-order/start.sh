#!/bin/bash
# =====================================================
# NexusMall Order Service - Docker 快速启动脚本
# =====================================================
# 用途：一键启动/停止开发环境
# 使用方式：
#   ./start.sh          # 启动所有服务
#   ./start.sh order    # 只启动 order 服务
#   ./stop.sh           # 停止所有服务
# =====================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 脚本目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查 Docker 是否安装
check_docker() {
    if ! command -v docker &> /dev/null; then
        log_error "Docker 未安装，请先安装 Docker"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose 未安装，请先安装 Docker Compose"
        exit 1
    fi
    
    log_success "Docker 环境检查通过"
}

# 创建必要目录
create_directories() {
    log_info "创建必要目录..."
    mkdir -p logs
    mkdir -p config
    mkdir -p init-db
    mkdir -p rocketmq
    
    log_success "目录创建完成"
}

# 初始化数据库
init_database() {
    log_info "检查数据库初始化脚本..."
    
    if [ ! -f "init-db/init.sql" ]; then
        log_warning "未找到数据库初始化脚本，创建示例脚本..."
        cat > init-db/init.sql << 'EOF'
-- NexusMall Order Database Initialization
CREATE DATABASE IF NOT EXISTS `nexusmall_order` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `nexusmall_order`;

-- Create undo_log table for Seata
CREATE TABLE IF NOT EXISTS `undo_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch_id` bigint(20) NOT NULL,
  `xid` varchar(100) NOT NULL,
  `context` varchar(128) NOT NULL,
  `rollback_info` longblob NOT NULL,
  `log_status` int(11) NOT NULL,
  `log_created` datetime NOT NULL,
  `log_modified` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

-- Create Nacos database
CREATE DATABASE IF NOT EXISTS `nacos_config` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `nacos_config`;

-- Nacos tables (simplified version)
CREATE TABLE IF NOT EXISTS `config_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `data_id` varchar(255) NOT NULL,
  `group_id` varchar(255) DEFAULT NULL,
  `content` longtext NOT NULL,
  `md5` varchar(32) DEFAULT NULL,
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `src_user` text,
  `src_ip` varchar(20) DEFAULT NULL,
  `app_name` varchar(128) DEFAULT NULL,
  `tenant_id` varchar(128) DEFAULT '',
  `c_desc` varchar(256) DEFAULT NULL,
  `c_use` varchar(64) DEFAULT NULL,
  `effect` varchar(64) DEFAULT NULL,
  `type` varchar(64) DEFAULT NULL,
  `c_schema` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_configinfo_datagrouptenant` (`data_id`,`group_id`,`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
EOF
        log_success "数据库初始化脚本已创建"
    else
        log_success "数据库初始化脚本已存在"
    fi
}

# 启动所有服务
start_all() {
    log_info "启动所有服务..."
    
    # 预检查
    check_docker
    create_directories
    init_database
    
    # 启动
    docker-compose up -d
    
    # 等待服务启动
    log_info "等待服务启动 (约 60 秒)..."
    sleep 60
    
    # 检查服务状态
    log_info "检查服务状态..."
    docker-compose ps
    
    # 显示访问信息
    echo ""
    log_success "====== 服务启动完成 ======"
    echo ""
    echo "Order 服务：     http://localhost:11000"
    echo "健康检查：       http://localhost:11000/actuator/health"
    echo "Prometheus:      http://localhost:11000/actuator/prometheus"
    echo "MySQL:          localhost:3306 (root/root123456)"
    echo "Redis:          localhost:6379 (密码：redis123456)"
    echo "Nacos:          http://localhost:8848/nacos (nacos/nacos)"
    echo "Sentinel:       http://localhost:8858 (sentinel/sentinel)"
    echo "RocketMQ Dashboard: http://localhost:8080"
    echo ""
    log_info "查看日志：docker-compose logs -f nexusmall-order"
    log_info "停止服务：./stop.sh"
}

# 停止所有服务
stop_all() {
    log_info "停止所有服务..."
    docker-compose down
    log_success "服务已停止"
}

# 重启所有服务
restart_all() {
    stop_all
    sleep 5
    start_all
}

# 查看日志
view_logs() {
    local service=$1
    if [ -z "$service" ]; then
        docker-compose logs -f
    else
        docker-compose logs -f "$service"
    fi
}

# 清理数据
clean_data() {
    log_warning "警告：此操作将删除所有数据卷!"
    read -p "确认继续？(y/N): " confirm
    if [ "$confirm" = "y" ] || [ "$confirm" = "Y" ]; then
        docker-compose down -v
        rm -rf logs/* config/* init-db/*
        log_success "数据已清理"
    else
        log_info "操作已取消"
    fi
}

# 显示帮助
show_help() {
    echo "用法：$0 [command]"
    echo ""
    echo "Commands:"
    echo "  start       启动所有服务 (默认)"
    echo "  stop        停止所有服务"
    echo "  restart     重启所有服务"
    echo "  logs        查看所有日志"
    echo "  logs [svc]  查看指定服务日志"
    echo "  clean       清理所有数据"
    echo "  help        显示帮助信息"
    echo ""
}

# 主程序
case "${1:-start}" in
    start)
        start_all
        ;;
    stop)
        stop_all
        ;;
    restart)
        restart_all
        ;;
    logs)
        view_logs "$2"
        ;;
    clean)
        clean_data
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        log_error "未知命令：$1"
        show_help
        exit 1
        ;;
esac
