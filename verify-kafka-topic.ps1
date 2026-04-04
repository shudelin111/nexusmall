# =====================================================
# 验证并创建 Kafka Topic - app-logs
# =====================================================

$KAFKA_BOOTSTRAP_SERVER = "10.10.1.40:31000"
$TOPIC_NAME = "app-logs"
$PARTITIONS = 3
$REPLICATION_FACTOR = 1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Kafka Topic 验证工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Bootstrap Server: $KAFKA_BOOTSTRAP_SERVER"
Write-Host "Topic Name: $TOPIC_NAME"
Write-Host ""

# 方法 1: 使用 kafka-topics.sh (如果 Docker 可用)
Write-Host "尝试验证 topic 是否存在..." -ForegroundColor Yellow

try {
    # 尝试通过 telnet 或 Test-NetConnection 验证 Kafka 是否可访问
    $connectionTest = Test-NetConnection -ComputerName 10.10.1.40 -Port 31000 -InformationLevel Quiet
    
    if ($connectionTest) {
        Write-Host "✓ Kafka 服务器连接成功" -ForegroundColor Green
        
        # 提示用户手动执行命令
        Write-Host "`n========================================" -ForegroundColor Cyan
        Write-Host "解决方案：" -ForegroundColor Cyan
        Write-Host "========================================" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "由于无法直接执行 Docker 命令，请按以下步骤操作：" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "1. SSH 登录到 Kafka 服务器 (10.10.1.40):" -ForegroundColor White
        Write-Host "   ssh root@10.10.1.40" -ForegroundColor Gray
        Write-Host ""
        Write-Host "2. 查看当前所有 topics:" -ForegroundColor White
        Write-Host "   docker exec -it <kafka-container-id> kafka-topics.sh --bootstrap-server localhost:9092 --list" -ForegroundColor Gray
        Write-Host ""
        Write-Host "3. 检查 app-logs topic 是否存在:" -ForegroundColor White
        Write-Host "   docker exec -it <kafka-container-id> kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic app-logs" -ForegroundColor Gray
        Write-Host ""
        Write-Host "4. 如果 topic 不存在，创建它:" -ForegroundColor White
        Write-Host "   docker exec -it <kafka-container-id> kafka-topics.sh --create \`" -ForegroundColor Gray
        Write-Host "     --bootstrap-server localhost:9092 \`" -ForegroundColor Gray
        Write-Host "     --replication-factor $REPLICATION_FACTOR \`" -ForegroundColor Gray
        Write-Host "     --partitions $PARTITIONS \`" -ForegroundColor Gray
        Write-Host "     --topic $TOPIC_NAME" -ForegroundColor Gray
        Write-Host ""
        Write-Host "5. 验证创建成功:" -ForegroundColor White
        Write-Host "   docker exec -it <kafka-container-id> kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic $TOPIC_NAME" -ForegroundColor Gray
        Write-Host ""
        
        Write-Host "========================================" -ForegroundColor Cyan
        Write-Host "或者，如果你有 Kafka 管理界面 (如 Kafka Manager, AKHQ 等)" -ForegroundColor Cyan
        Write-Host "可以直接在 Web 界面中创建 topic" -ForegroundColor Cyan
        Write-Host "========================================" -ForegroundColor Cyan
        
    } else {
        Write-Host "✗ Kafka 服务器连接失败" -ForegroundColor Red
        Write-Host "请检查 Kafka 服务是否正常运行" -ForegroundColor Yellow
    }
} catch {
    Write-Host "发生错误：$_" -ForegroundColor Red
}

Write-Host ""
Write-Host "按任意键退出..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
