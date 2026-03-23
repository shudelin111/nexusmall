docker run -d \
--name nacos \
-p 8848:8848 \
-p 9848:9848 \
-p 9849:9849 \
--restart always \
-e MODE=standalone \
nacos/nacos-server:v2.2.3