mkdir -p /mydata/nacos/logs
mkdir -p /mydata/nacos/conf


docker run -d \
--name nacos \
-p 8848:8848 \
-p 9848:9848 \
-p 9849:9849 \
--restart always \
-v /mydata/nacos/logs:/home/nacos/logs \
-e MODE=standalone \
-e SPRING_DATASOURCE_PLATFORM=mysql \
-e MYSQL_SERVICE_HOST=10.10.1.1 \
-e MYSQL_SERVICE_PORT=3306 \
-e MYSQL_SERVICE_DB_NAME=nacos_config \
-e MYSQL_SERVICE_USER=root \
-e MYSQL_SERVICE_PASSWORD=123456 \
nacos/nacos-server:v2.2.3





docker run -d \
--name nacos \
-p 8848:8848 \
-p 9848:9848 \
-p 9849:9849 \
--restart always \
-e MODE=standalone \
nacos/nacos-server:v2.2.3