# 创建数据挂载目录
mkdir -p /mydata/redis/data
# 创建配置挂载目录
mkdir -p /mydata/redis/conf



# 创建配置文件
touch /mydata/redis/conf/redis.conf

# 在里面写入一些基础配置（比如允许外网访问、设置密码）
cat <<EOF > /mydata/redis/conf/redis.conf
bind 0.0.0.0
protected-mode no
appendonly yes
requirepass 123456
EOF



docker run -d \
--name redis \
-p 6379:6379 \
--restart always \
-v /mydata/redis/data:/data \
-v /mydata/redis/conf/redis.conf:/etc/redis/redis.conf \
redis:latest \
redis-server /etc/redis/redis.conf