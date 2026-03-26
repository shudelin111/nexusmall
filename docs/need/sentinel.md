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