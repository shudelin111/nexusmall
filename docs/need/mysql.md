mkdir -p /mnt/sata1-4/docker/mysql/conf
mkdir -p /mnt/sata1-4/docker/mysql/data
mkdir -p /mnt/sata1-4/docker/mysql/log


cat <<EOF > /mnt/sata1-4/docker/mysql/conf/my.cnf
[mysqld]
user=mysql
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci
# 禁用符号链接，提高安全性
symbolic-links=0
# 设置时区为东八区
default-time-zone='+8:00'
# 允许最大连接数
max_connections=200
# 忽略表名大小写（Java 开发常用）
lower_case_table_names=1

[client]
default-character-set=utf8mb4

[mysql]
default-character-set=utf8mb4
EOF


docker run -d \
--name mysql \
--network rocketmq \
--restart=always \
-p 3306:3306 \
-e MYSQL_ROOT_PASSWORD=123456 \
-v /mnt/sata1-4/docker/mysql/conf:/etc/mysql/conf.d \
-v /mnt/sata1-4/docker/mysql/data:/var/lib/mysql \
-v /mnt/sata1-4/docker/mysql/log:/var/log/mysql \
mysql:5.7



-- 将 root 用户的密码改为新密码
ALTER USER 'root'@'localhost' IDENTIFIED BY '123456';

-- 如果你想确保从电脑（远程）也能用这个新密码登录
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY '123456' WITH GRANT OPTION;

-- 刷新权限使其生效
FLUSH PRIVILEGES;



#将 MySQL 加入已有的网络(rocketmq是之前定义的docker内部网络)
docker network connect rocketmq mysql