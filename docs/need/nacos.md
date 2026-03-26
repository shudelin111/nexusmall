mkdir -p /mnt/sata1-4/docker/nacos/logs
mkdir -p /mnt/sata1-4/docker/nacos/conf


#先生成一个临时的docker拿到nacos-logback.xml文件放到/mnt/sata1-4/docker/nacos/conf下
docker run -d --name nacos-tmp nacos/nacos-server:v2.2.3
docker cp nacos-tmp:/home/nacos/conf/. /mnt/sata1-4/docker/nacos/conf/
# 应该看到 application.properties, nacos-logback.xml 等文件
ls /mnt/sata1-4/docker/nacos/conf/
docker stop nacos-tmp
docker rm -f nacos-tmp

docker run -d \
--name nacos \
--network rocketmq \
--restart=always \
-p 8848:8848 -p 9848:9848 -p 9849:9849 \
-e MODE=standalone \
-e SPRING_DATASOURCE_PLATFORM=mysql \
-e MYSQL_SERVICE_HOST=mysql \
-e MYSQL_SERVICE_PORT=3306 \
-e MYSQL_SERVICE_DB_NAME=nacos_config \
-e MYSQL_SERVICE_USER=root \
-e MYSQL_SERVICE_PASSWORD=123456 \
-e NACOS_AUTH_IDENTITY_KEY=nacos \
-e NACOS_AUTH_IDENTITY_VALUE=nacos \
-e NACOS_AUTH_ENABLE=true \
-e NACOS_AUTH_TOKEN=SecretKey012345678901234567890123456789012345678901234567890123456789 \
-v /mnt/sata1-4/docker/nacos/logs:/home/nacos/logs \
-v /mnt/sata1-4/docker/nacos/conf:/home/nacos/conf \
-e JVM_XMS=256m \
-e JVM_XMX=512m \
nacos/nacos-server:v2.2.3

#内存不足导致挂了
root@iStoreOS:~# docker inspect nacos | grep -i "OOMKilled"
"OOMKilled": true,