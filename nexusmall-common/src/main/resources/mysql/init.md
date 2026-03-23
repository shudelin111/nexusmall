mkdir -p /mydata/mysql/log
mkdir -p /mydata/mysql/data
mkdir -p /mydata/mysql/conf

cat <<EOF > /mydata/mysql/conf/my.cnf
[client]
default-character-set=utf8mb4

[mysql]
default-character-set=utf8mb4

[mysqld]
init_connect='SET collation_connection = utf8mb4_unicode_ci'
init_connect='SET NAMES utf8mb4'
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci
skip-character-set-client-handshake
skip-name-resolve
EOF


docker run -d \
--name mysql \
-p 3306:3306 \
--restart always \
-v /mydata/mysql/conf/my.cnf:/etc/mysql/my.cnf \
-v /mydata/mysql/log:/var/log/mysql \
-v /mydata/mysql/data:/var/lib/mysql \
-e MYSQL_ROOT_PASSWORD=123456 \
mysql:5.7