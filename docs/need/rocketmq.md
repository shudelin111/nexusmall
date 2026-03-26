
docker network create rocketmq
# 创建 NameServer 目录
mkdir -p /mnt/sata1-4/docker/rocketmq/namesrv/logs /mnt/sata1-4/docker/rocketmq/namesrv/store

# 创建 Broker 目录和配置目录
mkdir -p /mnt/sata1-4/docker/rocketmq/broker/logs /mnt/sata1-4/docker/rocketmq/broker/store /mnt/sata1-4/docker/rocketmq/broker/conf


cat <<EOF > /mnt/sata1-4/docker/rocketmq/broker/conf/broker.conf
brokerClusterName = DefaultCluster
brokerName = broker-a
brokerId = 0
deleteWhen = 04
fileReservedTime = 48
brokerRole = ASYNC_MASTER
flushDiskType = ASYNC_FLUSH
namesrvAddr = rmqnamesrv:9876
# ！！！请把下面这个 IP 改成你软路由的局域网 IP ！！！
brokerIP1 = 10.10.1.1
EOF

docker run -d \
--name rmqnamesrv \
--network rocketmq \
--restart=always \
-p 9876:9876 \
-v /mnt/sata1-4/docker/rocketmq/namesrv/logs:/home/rocketmq/logs \
-v /mnt/sata1-4/docker/rocketmq/namesrv/store:/home/rocketmq/store \
-e "JAVA_OPT_EXT=-Xms256M -Xmx256M -Xmn128M" \
apache/rocketmq:4.9.7 \
sh mqnamesrv

docker run -d \
--name rmqbroker \
--network rocketmq \
--restart=always \
-p 10911:10911 -p 10909:10909 \
-v /mnt/sata1-4/docker/rocketmq/broker/logs:/home/rocketmq/logs \
-v /mnt/sata1-4/docker/rocketmq/broker/store:/home/rocketmq/store \
-v /mnt/sata1-4/docker/rocketmq/broker/conf/broker.conf:/home/rocketmq/conf/broker.conf \
-e "NAMESRV_ADDR=rmqnamesrv:9876" \
-e "JAVA_OPT_EXT=-Xms512M -Xmx512M -Xmn256M" \
apache/rocketmq:4.9.7 \
sh mqbroker -c /home/rocketmq/conf/broker.conf



docker run -d \
--name rmqdashboard \
--network rocketmq \
--restart=always \
-p 8082:8082 \
-e "NAMESRV_ADDR=rmqnamesrv:9876" \
-e "JAVA_OPTS=-Xms128M -Xmx256M" \
apacherocketmq/rocketmq-dashboard:1.0.0



docker exec -it rmqbroker sh mqadmin updateTopic -n rmqnamesrv:9876 -c DefaultCluster -t stock-topic
docker exec -it rmqbroker sh mqadmin updateTopic -n rmqnamesrv:9876 -c DefaultCluster -t order-topic
docker exec -it rmqbroker sh mqadmin updateTopic -n rmqnamesrv:9876 -c DefaultCluster -t user-behavior-topic