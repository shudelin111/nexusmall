@echo off
echo ========================================
echo   Kafka 连接测试
echo ========================================
echo.

set CLASSPATH=nexusmall-common/target/classes;nexusmall-common/target/test-classes

echo 正在启动 Kafka 连接测试...
echo.

java -cp "%CLASSPATH%" com.nexusmall.common.test.KafkaConnectionTest

echo.
echo 测试完成！
pause
