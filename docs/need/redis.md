mkdir -p /mnt/sata1-4/docker/redis/data
mkdir -p /mnt/sata1-4/docker/redis/conf
chmod -R 777 /mnt/sata1-4/docker/redis


cat <<EOF > /mnt/sata1-4/docker/redis/conf/redis.conf
# 允许远程连接
bind 0.0.0.0
# 关闭保护模式
protected-mode no
# 监听端口
port 6379
# 设置密码（同步你的全家桶密码）
requirepass 123456
# 开启 AOF 持久化（保证数据安全）
appendonly yes
# 限制最大内存（针对你的 3.7GB 环境，给 Redis 分配 256MB 足够开发用）
maxmemory 256mb
# 达到内存上限后的淘汰策略（清理最久没用的数据）
maxmemory-policy allkeys-lru
EOF

docker run -d \
--name redis \
--network rocketmq \
--restart=always \
-p 6379:6379 \
--memory 300m \
-v /mnt/sata1-4/docker/redis/data:/data \
-v /mnt/sata1-4/docker/redis/conf/redis.conf:/etc/redis/redis.conf \
redis:latest \
redis-server /etc/redis/redis.conf