mkdir -p /mnt/sata1-4/docker/sentinel/logs
chmod -R 777 /mnt/sata1-4/docker/sentinel/logs

docker run -d \
--name sentinel-dashboard \
--network rocketmq \
--restart=always \
-p 8858:8858 \
--memory 300m \
-v /mnt/sata1-4/docker/sentinel/logs:/root/logs \
-e SERVER_PORT=8858 \
-e JAVA_OPTS="-Xms128M -Xmx256M" \
bladex/sentinel-dashboard:1.8.6


flow
{
"resource": "/order/create",         // 接口路径
"grade": 1,                          // 控制类型：1=QPS
"count": 100,                        // 每秒最多 100 个请求
"limitApp": "default"                // 针对所有调用者
}
degrade
{
"resource": "createOrder",           // 资源名（方法名或 URL）
"grade": 1,                          // 熔断策略：1=异常比例
"count": 0.5,                        // 异常比例超过 50% 触发
"timeWindow": 60,                    // 熔断持续时间 60 秒
"minRequestAmount": 5,               // 最小请求数（达到此数量才统计）
"statIntervalMs": 10000,             // 统计时长
"limitApp": "default",               // 争对所有调用者
}