# NexusMall Order Service - Docker 部署指南

## 📋 目录

- [快速开始](#快速开始)
- [技术架构](#技术架构)
- [配置文件说明](#配置文件说明)
- [本地开发](#本地开发)
- [生产部署](#生产部署)
- [CI/CD 流水线](#cicd-流水线)
- [常见问题](#常见问题)

---

## 🚀 快速开始

### 前置要求

- **Docker**: 20.10+
- **Docker Compose**: 2.0+
- **内存**: 至少 8GB 可用内存
- **磁盘**: 至少 10GB 可用空间

### 一键启动 (推荐)

**Windows:**
```bash
start.bat
```

**Linux/Mac:**
```bash
chmod +x start.sh
./start.sh
```

启动后访问:
- **Order 服务**: http://localhost:11000
- **健康检查**: http://localhost:11000/actuator/health
- **Nacos 控制台**: http://localhost:8848/nacos (账号密码：nacos/nacos)
- **Sentinel 控制台**: http://localhost:8858 (账号密码：sentinel/sentinel)

---

## 🏗️ 技术架构

### Docker 镜像设计

采用**多阶段构建**(Multi-stage Build),实现极致优化:

```
┌─────────────────────────────────────┐
│  Stage 1: Builder (Maven)           │
│  - 编译源码                          │
│  - 下载依赖                          │
│  - 打包 JAR (约 500MB)               │
└─────────────────────────────────────┘
              ↓ COPY
┌─────────────────────────────────────┐
│  Stage 2: Runtime (JRE Slim)        │
│  - 仅包含 JRE 运行环境                │
│  - 最终镜像约 200MB (减少 60%+)      │
│  - 非 root 用户运行                   │
│  - JVM 容器感知参数                  │
└─────────────────────────────────────┘
```

### 镜像特点

✅ **安全加固**: 非 root 用户运行  
✅ **性能优化**: G1GC + 容器感知 JVM 参数  
✅ **优雅关闭**: SIGTERM 信号处理  
✅ **健康检查**: Actuator 端点检测  
✅ **日志收集**: JSON 格式化输出  

---

## 📁 配置文件说明

### 目录结构

```
nexusmall-order/
├── Dockerfile                 # 生产级 Dockerfile
├── .dockerignore             # 构建上下文过滤
├── docker-compose.yml        # 开发环境编排
├── rocketmq/
│   └── broker.conf          # RocketMQ Broker 配置
├── init-db/
│   └── init.sql             # 数据库初始化脚本
├── logs/                     # 日志目录 (运行时生成)
├── config/                   # 外部配置目录 (可选)
├── start.sh                  # Linux/Mac启动脚本
└── start.bat                 # Windows 启动脚本
```

### 环境变量配置

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| `SPRING_PROFILES_ACTIVE` | `prod` | Spring Boot 激活的 profile |
| `SERVER_PORT` | `11000` | 应用服务端口 |
| `JAVA_OPTS` | 见 Dockerfile | JVM 启动参数 |
| `TZ` | `Asia/Shanghai` | 时区设置 |

---

## 💻 本地开发

### 方式一：Docker Compose (推荐)

```bash
# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f nexusmall-order

# 停止服务
docker-compose down

# 清理数据卷 (谨慎使用!)
docker-compose down -v
```

### 方式二：单独运行 Order 服务

```bash
# 先确保 MySQL、Redis、Nacos 已启动
docker-compose up -d mysql redis nacos

# 构建镜像
docker build -t nexusmall/order:latest -f Dockerfile ..

# 运行容器
docker run -d \
  --name nexusmall-order \
  -p 11000:11000 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e SPRING_CLOUD_NACOS_DISCOVERY_SERVER_ADDR=localhost:8848 \
  nexusmall/order:latest
```

### 调试模式

启用远程调试 (JDWP):

```bash
docker run -d \
  --name nexusmall-order \
  -p 11000:11000 \
  -p 5005:5005 \
  -e JAVA_OPTS="-Xdebug -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" \
  nexusmall/order:latest
```

IDEA 连接配置:
- **Host**: localhost
- **Port**: 5005
- **Transport**: Socket

---

## 🌐 生产部署

### Kubernetes 部署

创建 Deployment:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nexusmall-order
  namespace: nexusmall-prod
spec:
  replicas: 3
  selector:
    matchLabels:
      app: nexusmall-order
  template:
    metadata:
      labels:
        app: nexusmall-order
        version: v1
    spec:
      containers:
      - name: nexusmall-order
        image: registry.cn-shanghai.aliyuncs.com/nexusmall/nexusmall-order:v1.0.0
        ports:
        - containerPort: 11000
          name: http
        - containerPort: 11001
          name: management
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: JAVA_OPTS
          value: >-
            -XX:MaxRAMPercentage=75.0
            -XX:InitialRAMPercentage=50.0
            -XX:+UseContainerSupport
            -XX:+UseG1GC
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "2000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 11000
          initialDelaySeconds: 60
          periodSeconds: 30
          timeoutSeconds: 10
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 11000
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        volumeMounts:
        - name: logs
          mountPath: /tmp/logs
      volumes:
      - name: logs
        emptyDir: {}
```

### HPA 自动扩缩容

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: nexusmall-order-hpa
  namespace: nexusmall-prod
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: nexusmall-order
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

---

## 🔧 CI/CD 流水线

### GitLab CI/CD

项目根目录已包含 `.gitlab-ci.yml`,支持:

1. **自动触发**: 
   - Merge Request → 编译 + 单元测试
   - Develop 分支 → 部署开发环境
   - Main 分支 → 部署测试环境
   - Tag 发布 → 部署生产环境

2. **质量门禁**:
   - 单元测试覆盖率检查
   - Checkstyle/PMD/SpotBugs 代码分析
   - Docker 镜像安全扫描 (Trivy)

3. **部署策略**:
   - 开发环境：自动部署
   - 测试环境：自动部署 + 冒烟测试
   - 生产环境：手动确认 + 蓝绿部署

### 配置 CI/CD 变量

在 GitLab 项目设置中配置以下变量:

```yaml
DOCKER_REGISTRY_USER:     # Docker Registry 用户名
DOCKER_REGISTRY_PASSWORD: # Docker Registry 密码
DB_ROOT_PASSWORD:         # 数据库 root 密码
KUBECONFIG:               # Kubernetes 配置 (Base64 编码)
```

### Jenkins Pipeline 示例

```groovy
pipeline {
    agent any
    
    tools {
        maven 'Maven 3.8.8'
        jdk 'JDK 17'
    }
    
    environment {
        DOCKER_IMAGE = "registry.cn-shanghai.aliyuncs.com/nexusmall/nexusmall-order"
        VERSION = "${env.BUILD_NUMBER}"
    }
    
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/nexusmall/nexusmall.git'
            }
        }
        
        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests -pl nexusmall-order -am'
            }
        }
        
        stage('Test') {
            steps {
                sh 'mvn test -pl nexusmall-order'
            }
            post {
                always {
                    junit 'nexusmall-order/target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("${DOCKER_IMAGE}:${VERSION}", "-f nexusmall-order/Dockerfile .")
                }
            }
        }
        
        stage('Push Image') {
            steps {
                script {
                    docker.withRegistry('https://registry.cn-shanghai.aliyuncs.com', 'aliyun-docker-credentials') {
                        docker.image("${DOCKER_IMAGE}:${VERSION}").push()
                        docker.image("${DOCKER_IMAGE}:${VERSION}").push('latest')
                    }
                }
            }
        }
        
        stage('Deploy to K8s') {
            steps {
                sh '''
                    kubectl set image deployment/nexusmall-order nexusmall=${DOCKER_IMAGE}:${VERSION} -n nexusmall-prod
                    kubectl rollout status deployment/nexusmall-order -n nexusmall-prod
                '''
            }
        }
    }
}
```

---

## ❓ 常见问题

### 1. 容器启动失败，日志显示 "Connection refused"

**原因**: 依赖服务 (MySQL/Nacos) 未就绪

**解决**:
```bash
# 检查依赖服务状态
docker-compose ps

# 等待所有服务健康后再启动 order
docker-compose up -d mysql redis nacos
sleep 30
docker-compose up -d nexusmall-order
```

### 2. OOMKilled (内存不足)

**原因**: JVM 堆内存设置过大

**解决**:
```yaml
# docker-compose.yml 中调整
environment:
  - JAVA_OPTS=-XX:MaxRAMPercentage=50.0  # 降低为 50%
```

### 3. Nacos 注册失败

**检查项**:
- Nacos 是否正常运行 (http://localhost:8848/nacos)
- 服务名是否正确 (`nexusmall-order`)
- 命名空间是否匹配

### 4. Seata 事务不生效

**检查项**:
- `undo_log` 表是否创建
- Seata Server 是否启动
- XID 是否正确传递

### 5. 镜像构建缓慢

**优化建议**:
- 使用 `.dockerignore` 过滤不必要文件
- Maven 依赖缓存 (CI 中配置 cache)
- 多阶段构建分离编译和运行

---

## 📊 监控与日志

### Prometheus 指标

访问: http://localhost:11000/actuator/prometheus

关键指标:
- `jvm_memory_used_bytes`: JVM 内存使用
- `http_server_requests_seconds`: HTTP 请求延迟
- `seata_global_transaction`: 分布式事务统计

### Grafana 仪表盘

导入 Dashboard ID: `11000` (Spring Boot 统计)

### 日志查询

```bash
# 实时日志
docker-compose logs -f nexusmall-order

# 最近 100 行
docker-compose logs --tail=100 nexusmall-order

# 错误日志
docker-compose logs nexusmall-order | grep ERROR
```

---

## 🔗 相关链接

- [Docker 官方文档](https://docs.docker.com/)
- [Spring Boot Docker 指南](https://spring.io/guides/topicals/spring-boot-docker/)
- [GitLab CI/CD 最佳实践](https://docs.gitlab.com/ee/ci/)
- [Kubernetes 部署指南](https://kubernetes.io/docs/home/)

---

**最后更新**: 2026-04-03  
**维护团队**: NexusMall DevOps Team
