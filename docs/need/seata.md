# 创建日志目录
mkdir -p /mnt/sata1-4/docker/seata/logs
# 给权限，防止 Docker 写入失败
chmod -R 777 /mnt/sata1-4/docker/seata/logs


-- 创建数据库
CREATE DATABASE IF NOT EXISTS `seata` DEFAULT CHARACTER SET utf8mb4;
USE `seata`;

-- 创建全局事务表 (TC记录全局事务状态)
CREATE TABLE `global_table` (
`xid` varchar(128) NOT NULL,
`transaction_id` bigint(20) DEFAULT NULL,
`status` tinyint(4) NOT NULL,
`application_id` varchar(32) DEFAULT NULL,
`transaction_service_group` varchar(32) DEFAULT NULL,
`transaction_name` varchar(128) DEFAULT NULL,
`timeout` int(11) DEFAULT NULL,
`begin_time` bigint(20) DEFAULT NULL,
`application_data` varchar(2000) DEFAULT NULL,
`gmt_create` datetime DEFAULT NULL,
`gmt_modified` datetime DEFAULT NULL,
PRIMARY KEY (`xid`),
KEY `idx_gmt_modified_status` (`gmt_modified`,`status`),
KEY `idx_transaction_id` (`transaction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建分支事务表 (记录分支状态)
CREATE TABLE `branch_table` (
`branch_id` bigint(20) NOT NULL,
`xid` varchar(128) NOT NULL,
`transaction_id` bigint(20) DEFAULT NULL,
`resource_group_id` varchar(32) DEFAULT NULL,
`resource_id` varchar(256) DEFAULT NULL,
`branch_type` varchar(8) DEFAULT NULL,
`status` tinyint(4) DEFAULT NULL,
`biz_id` varchar(64) DEFAULT NULL,
`comment` varchar(255) DEFAULT NULL,
`gmt_create` datetime(6) DEFAULT NULL,
`gmt_modified` datetime(6) DEFAULT NULL,
PRIMARY KEY (`branch_id`),
KEY `idx_xid` (`xid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建全局锁表 (防止脏读写)
CREATE TABLE `lock_table` (
`row_key` varchar(128) NOT NULL,
`xid` varchar(128) DEFAULT NULL,
`transaction_id` bigint(20) DEFAULT NULL,
`branch_id` bigint(20) DEFAULT NULL,
`resource_id` varchar(256) DEFAULT NULL,
`table_name` varchar(32) DEFAULT NULL,
`pk` varchar(36) DEFAULT NULL,
`gmt_create` datetime DEFAULT NULL,
`gmt_modified` datetime DEFAULT NULL,
PRIMARY KEY (`row_key`),
KEY `idx_xid` (`xid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;




docker rm -f seata-server

docker run -d \
--name seata-server \
--network rocketmq \
--restart=always \
-p 8091:8091 \
--memory 512m \
-e SEATA_IP=10.10.1.1 \
-e SEATA_PORT=8091 \
-e STORE_MODE=db \
-e DB_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver \
-e DB_URL="jdbc:mysql://mysql:3306/seata?useUnicode=true&rewriteBatchedStatements=true&useSSL=false" \
-e DB_USER=root \
-e DB_PASSWORD=123456 \
-e REGISTRY_TYPE=nacos \
-e REGISTRY_NACOS_SERVER_ADDR=nacos:8848 \
-e CONFIG_TYPE=nacos \
-e CONFIG_NACOS_SERVER_ADDR=nacos:8848 \
-v /mnt/sata1-4/docker/seata/logs:/root/logs \
-e "JAVA_OPTS=-Xms128M -Xmx384M -Xmn128M -XX:MaxDirectMemorySize=64M -XX:+UseG1GC" \
seataio/seata-server:1.4.2


第五步：Nacos 中的“握手”配置 (极重要)
Seata 客户端（你的 Java 代码）连接 TC（服务端）时，需要一个事务分组名。

在 Nacos 的配置列表中，新建一个配置：

Data ID: service.vgroupMapping.default_tx_group

Group: SEATA_GROUP (或者 DEFAULT_GROUP)

配置内容: default

这样你的 Java 项目配置 tx-service-group: default_tx_group 时，才能找到 Seata 服务端。



cat <<EOF > /mnt/sata1-4/docker/seata/config/registry.conf
registry {
type = "nacos"
nacos {
application = "seata-server"
serverAddr = "10.10.1.1:8848"
group = "SEATA_GROUP"
username = "nacos"
password = "nacos"
namespace = ""
cluster = "default"
}
}
config {
type = "nacos"
nacos {
serverAddr = "10.10.1.1:8848"
username = "nacos"
password = "nacos"
namespace = ""
group = "SEATA_GROUP"
dataId = "seataServer.properties"
}
}
EOF


docker run -d \
--name seata-server \
--network rocketmq \
--restart=always \
-p 8091:8091 \
--memory 512m \
-e SEATA_IP=10.10.1.1 \
-e SEATA_PORT=8091 \
-e STORE_MODE=db \
-e DB_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver \
-e DB_URL="jdbc:mysql://10.10.1.1:3306/seata?useUnicode=true&rewriteBatchedStatements=true&useSSL=false" \
-e DB_USER=root \
-e DB_PASSWORD=123456 \
-v /mnt/sata1-4/docker/seata/config/registry.conf:/seata-server/resources/registry.conf \
-v /mnt/sata1-4/docker/seata/logs:/root/logs \
-e JAVA_OPTS="-Xms128M -Xmx384M -Xmn128M -XX:MaxDirectMemorySize=64M -XX:+UseG1GC" \
seataio/seata-server:1.4.2