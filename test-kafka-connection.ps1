# Kafka 连接测试脚本
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Kafka 连接测试" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$bootstrapServers = "10.10.1.40:31000"
Write-Host "Kafka 服务器：$bootstrapServers" -ForegroundColor Yellow
Write-Host ""

# 测试 1: TCP 连接测试
Write-Host "[测试 1] TCP 连接测试..." -ForegroundColor Cyan
try {
    $tcpClient = New-Object System.Net.Sockets.TcpClient
    $connected = $tcpClient.ConnectAsync($bootstrapServers.Split(":")[0], [int]$bootstrapServers.Split(":")[1]).Wait(5000)
    
    if ($connected -and $tcpClient.Connected) {
        Write-Host "  ✓ TCP 连接成功！" -ForegroundColor Green
        Write-Host "  端口 $bootstrapServers 可访问" -ForegroundColor Green
    } else {
        Write-Host "  ✗ TCP 连接失败" -ForegroundColor Red
        Write-Host "  无法连接到 Kafka 服务器" -ForegroundColor Red
    }
    $tcpClient.Close()
} catch {
    Write-Host "  ✗ 连接异常：$($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  说明" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "完整的 Kafka 测试需要使用 Java 代码，因为涉及：" -ForegroundColor White
Write-Host "  1. Producer 发送消息测试" -ForegroundColor Gray
Write-Host "  2. Consumer 订阅消息测试" -ForegroundColor Gray
Write-Host "  3. AdminClient 管理功能测试（列出 topic）" -ForegroundColor Gray
Write-Host ""
Write-Host "测试类位置：" -ForegroundColor Yellow
Write-Host "  nexusmall-common/src/test/java/com/nexusmall/common/test/KafkaConnectionTest.java" -ForegroundColor White
Write-Host ""
Write-Host "运行方式：" -ForegroundColor Yellow
Write-Host "  方式 1: 在 IDEA 中右键运行 KafkaConnectionTest.main()" -ForegroundColor White
Write-Host "  方式 2: mvn test -pl nexusmall-common -Dtest=KafkaConnectionTest" -ForegroundColor White
Write-Host ""
