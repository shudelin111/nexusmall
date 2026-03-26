# RocketMQ 4.9.7 Docker 服务器部署脚本
# 服务器地址：10.10.1.1
# =====================================================

$serverIp = "10.10.1.1"
$sshUser = "root"

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "RocketMQ 4.9.7 服务器部署脚本" -ForegroundColor Cyan
Write-Host "服务器：$serverIp" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "请在 PowerShell 中执行以下命令：" -ForegroundColor Yellow
Write-Host ""

$commands = @(
    "# 1. SSH 登录服务器",
    "ssh $sshUser@$serverIp",
    "",
    "# 2. 拉取镜像",
    "docker pull apache/rocketmq:4.9.7",
    "",
    "# 3. 创建目录",
    "mkdir -p /opt/docker/rocketmq/namesrv",
    "mkdir -p /opt/docker/rocketmq/broker",
    "",
    "# 4. 清理旧容器",
    "docker stop rocketmq-broker rocketmq-namesrv 2>/dev/null",
    "docker rm rocketmq-broker rocketmq-namesrv 2>/dev/null",
    "",
    "# 5. 启动 NameServer",
    "docker run -d --name rocketmq-namesrv --restart always -p 9876:9876 -v /opt/docker/rocketmq/namesrv:/home/rocketmq/namesrv apache/rocketmq:4.9.7 sh mqnamesrv",
    "",
    "# 6. 等待 10 秒",
    "sleep 10",
    "",
    "# 7. 启动 Broker",
    "docker run -d --name rocketmq-broker --restart always -p 10911:10911 -p 10909:10909 -v /opt/docker/rocketmq/broker:/home/rocketmq/broker --link rocketmq-namesrv:namesrv -e NAMESRV_ADDR=namesrv:9876 -e 'JAVA_OPTS=-Xms256m -Xmx256m -XX:+UseG1GC' apache/rocketmq:4.9.7 sh mqbroker -c DefaultCluster",
    "",
    "# 8. 查看状态",
    "docker ps",
    "",
    "# 9. 进入容器创建 Topic",
    "docker exec -it rocketmq-broker bash",
    "",
    "# 10. 在容器内执行（每条命令回车）",
    "sh bin/mqadmin updateTopic -n namesrv:9876 -c DefaultCluster -t stock-topic",
    "sh bin/mqadmin updateTopic -n namesrv:9876 -c DefaultCluster -t order-topic",
    "sh bin/mqadmin updateTopic -n namesrv:9876 -c DefaultCluster -t user-behavior-topic",
    "exit",
    "",
    "# 11. 退出服务器",
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

Write-Host ""
Write-Host "==================================================" -ForegroundColor Green
Write-Host "完成以上步骤后，重启 nexusmall-product 服务即可" -ForegroundColor Green
Write-Host "==================================================" -ForegroundColor Green
