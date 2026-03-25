# RocketMQ 4.9.7 Docker 部署脚本（服务器：10.10.1.1）
# 使用方式：SSH 登录服务器后手动执行下方命令

$serverIp = "10.10.1.1"
$sshUser = "root"  # 根据你的实际用户名修改

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "RocketMQ 4.9.7 手动部署指南" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "`n请按以下步骤在服务器上执行：" -ForegroundColor Yellow

# 1. 拉取镜像
Write-Host "`n[1/5] 拉取 RocketMQ 镜像..." -ForegroundColor Yellow
ssh $sshUser@$serverIp "docker pull apache/rocketmq:4.9.7"

# 2. 创建目录
Write-Host "`n[2/5] 创建配置目录..." -ForegroundColor Yellow
ssh $sshUser@$serverIp "mkdir -p /opt/docker/rocketmq/namesrv && mkdir -p /opt/docker/rocketmq/broker"

# 3. 停止旧容器（如果存在）
Write-Host "`n[3/5] 清理旧容器..." -ForegroundColor Yellow
ssh $sshUser@$serverIp "docker stop rocketmq-broker rocketmq-namesrv 2>/dev/null; docker rm rocketmq-broker rocketmq-namesrv 2>/dev/null"

# 4. 启动 NameServer
Write-Host "`n[4/5] 启动 NameServer..." -ForegroundColor Yellow
ssh $sshUser@$serverIp "docker run -d --name rocketmq-namesrv --restart always -p 9876:9876 -v /opt/docker/rocketmq/namesrv:/home/rocketmq/namesrv apache/rocketmq:4.9.7 sh mqnamesrv"

# 等待 NameServer 启动完成（至少 10 秒）
Write-Host "等待 NameServer 启动..." -ForegroundColor Cyan
Start-Sleep -Seconds 10

# 5. 启动 Broker
Write-Host "`n[5/5] 启动 Broker..." -ForegroundColor Yellow
ssh $sshUser@$serverIp "docker run -d --name rocketmq-broker --restart always -p 10911:10911 -p 10909:10909 -v /opt/docker/rocketmq/broker:/home/rocketmq/broker --link rocketmq-namesrv:namesrv -e NAMESRV_ADDR=namesrv:9876 -e 'JAVA_OPTS=-Xms256m -Xmx256m -XX:+UseG1GC' apache/rocketmq:4.9.7 sh mqbroker -c /home/rocketmq/broker/broker.conf"

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "✓ 部署完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host "`nNameServer 地址：$serverIp`:9876" -ForegroundColor Cyan
Write-Host "Broker 地址：$serverIp`:10911" -ForegroundColor Cyan
Write-Host "`n查看日志：" -ForegroundColor Yellow
Write-Host "  docker logs -f rocketmq-namesrv" -ForegroundColor Gray
Write-Host "  docker logs -f rocketmq-broker" -ForegroundColor Gray
Write-Host "`n查看状态：" -ForegroundColor Yellow
Write-Host "  docker ps" -ForegroundColor Gray

# ============================================================
# 手动执行命令清单（复制粘贴到服务器终端）
# ============================================================

Write-Host "`n========================================" -ForegroundColor Magenta
Write-Host "📋 手动执行命令清单" -ForegroundColor Magenta
Write-Host "========================================" -ForegroundColor Magenta
Write-Host "`n1. SSH 登录服务器：" -ForegroundColor Cyan
Write-Host "   ssh $sshUser@$serverIp" -ForegroundColor Gray
Write-Host "`n2. 依次执行以下命令：" -ForegroundColor Cyan
Write-Host "`n--- 开始复制 ---" -ForegroundColor Green

$commands = @(
    "# 1. 拉取镜像",
    "docker pull apache/rocketmq:4.9.7",
    "",
    "# 2. 创建配置目录",
    "mkdir -p /opt/docker/rocketmq/namesrv",
    "mkdir -p /opt/docker/rocketmq/broker",
    "",
    "# 3. 停止并删除旧容器（如果存在）",
    "docker stop rocketmq-broker rocketmq-namesrv 2>/dev/null",
    "docker rm rocketmq-broker rocketmq-namesrv 2>/dev/null",
    "",
    "# 4. 启动 NameServer",
    "docker run -d --name rocketmq-namesrv --restart always -p 9876:9876 -v /opt/docker/rocketmq/namesrv:/home/rocketmq/namesrv apache/rocketmq:4.9.7 sh mqnamesrv",
    "",
    "# 5. 等待 10 秒后启动 Broker",
    "sleep 10",
    "",
    "# 6. 启动 Broker",
    "docker run -d --name rocketmq-broker --restart always -p 10911:10911 -p 10909:10909 -v /opt/docker/rocketmq/broker:/home/rocketmq/broker --link rocketmq-namesrv:namesrv -e NAMESRV_ADDR=namesrv:9876 -e 'JAVA_OPTS=-Xms256m -Xmx256m -XX:+UseG1GC' apache/rocketmq:4.9.7 sh mqbroker -c /home/rocketmq/broker/broker.conf",
    "",
    "# 7. 查看容器状态",
    "docker ps",
    "",
    "# 8. 创建 Topic（进入容器执行）",
    "docker exec -it rocketmq-broker bash",
    "",
    "# 9. 在容器内执行以下 3 条命令：",
    "sh bin/mqadmin updateTopic -n namesrv:9876 -c DefaultCluster -t order-topic",
    "sh bin/mqadmin updateTopic -n namesrv:9876 -c DefaultCluster -t stock-topic",
    "sh bin/mqadmin updateTopic -n namesrv:9876 -c DefaultCluster -t user-behavior-topic",
    "",
    "# 10. 退出容器",
    "exit"
)

foreach ($cmd in $commands) {
    if ([string]::IsNullOrWhiteSpace($cmd)) {
        Write-Host ""
    } elseif ($cmd.StartsWith("#")) {
        Write-Host $cmd -ForegroundColor DarkGray
    } else {
        Write-Host $cmd -ForegroundColor White
    }
}

Write-Host "--- 结束复制 ---`n" -ForegroundColor Green

Write-Host "💡 提示：可以将上方命令复制粘贴到服务器终端执行！" -ForegroundColor Yellow


docker exec -it rocketmq-broker bash

# 在容器内执行
./mqadmin updateTopic -n namesrv:9876 -c DefaultCluster -t order-topic
./mqadmin updateTopic -n namesrv:9876 -c DefaultCluster -t stock-topic
./mqadmin updateTopic -n namesrv:9876 -c DefaultCluster -t user-behavior-topic















# 启动 NameServer
docker run -d \
  --name rocketmq-namesrv \
  --restart always \
  -p 9876:9876 \
  -v /opt/docker/rocketmq/namesrv:/home/rocketmq/namesrv \
  apache/rocketmq:4.9.7 sh mqnamesrv

docker run -d \
  --name rocketmq-broker \
  --restart always \
  -p 10911:10911 \
  -p 10909:10909 \
  -v /opt/docker/rocketmq/broker:/home/rocketmq/broker \
  --link rocketmq-namesrv:namesrv \
  -e "NAMESRV_ADDR=namesrv:9876" \
  -e "JAVA_OPTS=-Xms256m -Xmx256m -XX:+UseG1GC" \
  apache/rocketmq:4.9.7 sh mqbroker -c /home/rocketmq/broker/broker.conf



  # 1. 拉取镜像
  docker pull apache/rocketmq:4.9.7

  # 2. 创建配置目录
  mkdir -p /opt/docker/rocketmq/namesrv
  mkdir -p /opt/docker/rocketmq/broker

  # 3. 清理旧容器
  docker stop rocketmq-broker rocketmq-namesrv 2>/dev/null
  docker rm rocketmq-broker rocketmq-namesrv 2>/dev/null

  # 4. 启动 NameServer
  docker run -d --name rocketmq-namesrv --restart always -p 9876:9876 -v /opt/docker/rocketmq/namesrv:/home/rocketmq/namesrv apache/rocketmq:4.9.7 sh mqnamesrv

  # 5. 等待 10 秒
  sleep 10

  # 6. 启动 Broker
  docker run -d --name rocketmq-broker --restart always -p 10911:10911 -p 10909:10909 -v /opt/docker/rocketmq/broker:/home/rocketmq/broker --link rocketmq-namesrv:namesrv -e NAMESRV_ADDR=namesrv:9876 -e 'JAVA_OPTS=-Xms256m -Xmx256m -XX:+UseG1GC' apache/rocketmq:4.9.7 sh mqbroker -c /home/rocketmq/broker/broker.conf

  # 7. 查看状态
  docker ps

  # 8. 进入容器创建 Topic
  docker exec -it rocketmq-broker bash

  # 在容器内执行这 3 条命令：
  sh bin/mqadmin updateTopic -n namesrv:9876 -c DefaultCluster -t order-topic
  sh bin/mqadmin updateTopic -n namesrv:9876 -c DefaultCluster -t stock-topic
  sh bin/mqadmin updateTopic -n namesrv:9876 -c DefaultCluster -t user-behavior-topic

  # 退出容器
  exit





docker run -d \
--name nacos \
-p 8848:8848 \
-p 9848:9848 \
-p 9849:9849 \
--restart always \
-v /mydata/nacos/logs:/home/nacos/logs \
-e MODE=standalone \
-e "JVM_XMS=256m" \
-e "JVM_XMX=256m" \
-e "JVM_XMN=128m" \
-e "JVM_MS=64m" \
-e "JVM_MMS=128m" \
-e SPRING_DATASOURCE_PLATFORM=mysql \
-e MYSQL_SERVICE_HOST=10.10.1.1 \
-e MYSQL_SERVICE_PORT=3306 \
-e MYSQL_SERVICE_DB_NAME=nacos_config \
-e MYSQL_SERVICE_USER=root \
-e MYSQL_SERVICE_PASSWORD=123456 \
nacos/nacos-server:v2.2.3
