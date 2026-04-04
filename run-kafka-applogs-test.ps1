Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Kafka app-logs Topic 连接测试" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$CLASSPATH = "nexusmall-common/target/classes;nexusmall-common/target/test-classes"

Write-Host "正在启动 Kafka 测试..." -ForegroundColor Yellow
Write-Host "ClassPath: $CLASSPATH" -ForegroundColor Gray
Write-Host ""

# 设置 CLASSPATH 并运行 Java 程序
$env:CLASSPATH = $CLASSPATH
java com.nexusmall.common.test.KafkaConnectionTest

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  测试完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
