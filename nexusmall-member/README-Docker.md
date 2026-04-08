# NexusMall Member Service - Docker & CI/CD 指南

## 📋 目录

- [Docker 镜像构建](#docker-镜像构建)
- [本地开发环境](#本地开发环境)
- [CI/CD 流水线](#cicd-流水线)
- [Kubernetes 部署](#kubernetes-部署)
- [故障排查](#故障排查)

---

## 🐳 Docker 镜像构建

### 多阶段构建架构

Member 服务采用业界标准的**多阶段构建**方式，包含两个阶段：

1. **Builder Stage**: 使用 Maven 编译打包
2. **Runtime Stage**: 使用轻量级 JRE 运行应用

### 技术特性

✅ **镜像体积优化**: 相比传统构建减少 80%+  
✅ **依赖分层缓存**: 构建速度提升 50%+  
✅ **非 root 用户运行**: 安全合规  
✅ **JVM 容器感知**: 防止 OOM  
✅ **优雅关闭支持**: SIGTERM 信号处理  
✅ **健康检查**: 自动检测服务状态  

### 构建命令

```bash
# 在项目根目录执行
cd D:\IdeaProjects\nexusmall

# 构建镜像
docker build -t nexusmall-member:latest -f nexusmall-member/Dockerfile .

# 查看镜像大小
docker images | grep nexusmall-member

# 运行容器（测试）
docker run -d \
  --name nexusmall-member \
  -p 10000:10000 \
  -e SPRING_PROFILES_ACTIVE=dev \
  nexusmall-member:latest

# 查看日志
docker logs -f nexusmall-member

# 停止容器
docker stop nexusmall-member
```

### 环境变量配置

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| `TZ` | `Asia/Shanghai` | 时区设置 |
| `JAVA_OPTS` | (见 Dockerfile) | JVM 参数 |
| `SPRING_PROFILES_ACTIVE` | `prod` | Spring Profile |
| `SERVER_PORT` | `10000` | 服务端口 |
| `LOG_PATH` | `/tmp/logs` | 日志路径 |

---

## 💻 本地开发环境

### 使用 Docker Compose（可选）

如果需要完整的本地开发环境（MySQL + Redis + Nacos + RocketMQ），可以参考 Order 模块的 docker-compose.yml。

### 快速启动

```bash
# 1. 编译项目
mvn clean package -pl nexusmall-member -am -DskipTests

# 2. 运行 JAR
java -jar nexusmall-member/target/nexusmall-member-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=dev

# 3. 访问健康检查
curl http://localhost:10000/actuator/health
```

---

## 🚀 CI/CD 流水线

### 工作流程

Member 服务已集成到 GitHub Actions CI/CD 流水线中，采用**智能触发机制**：

```
代码推送 → 检测变更 → 编译测试 → 质量检查 → 构建镜像 → 安全扫描 → 更新 K8s Manifest → ArgoCD 自动部署
```

### 触发条件

#### 1. 自动触发

- **Push 到 dev/master 分支**且修改了以下文件：
  - `nexusmall-member/**`
  - `nexusmall-common/**`
  - `pom.xml`
  - `nexusmall-common/pom.xml`

#### 2. 手动触发

在 GitHub Actions 页面点击 "Run workflow" 即可手动触发。

### 流水线阶段

#### 阶段 1: 编译与测试 (`build-member`)

```yaml
- 安装父 POM 和 Common 模块
- 编译 Member 模块
- 运行单元测试（带 JaCoCo 覆盖率检查）
- 上传测试结果和覆盖率报告
```

**注意**: 当前单元测试已临时禁用（Nacos 为内网服务），后续需要恢复。

#### 阶段 2: 代码质量检查 (`quality-check-member`)

```yaml
- Checkstyle: 代码风格检查
- PMD: 代码缺陷检查
- SpotBugs: 静态分析
```

仅在 `dev` 和 `master` 分支执行。

#### 阶段 3: 构建 Docker 镜像 (`build-docker-member`)

```yaml
- 登录 Docker Hub
- 生成镜像标签：
  * dev 分支: latest, sha-{commit}
  * master 分支: sha-{commit}
  * Tag: v1.2.3, sha-{commit}
- 多阶段构建并推送
- 启用 Buildx 缓存加速
```

#### 阶段 4: 安全扫描 (`security-scan-member`)

```yaml
- Trivy 漏洞扫描
- 扫描级别: CRITICAL, HIGH, MEDIUM
- 忽略未修复漏洞
- 上传结果到 GitHub Security Tab
```

#### 阶段 5: 更新 K8s Manifest (`update-manifest-member`)

```yaml
- 根据分支选择标签策略：
  * dev: latest（开发环境自动拉取最新）
  * master: sha-{commit}（生产环境版本可追溯）
- 更新 deploy/member-service/deployment.yaml
- 提交并推送到 Git（触发 ArgoCD 同步）
- 重试机制: 最多 3 次，处理并行冲突
```

### 镜像标签策略

| 分支/事件 | 标签示例 | 用途 |
|-----------|----------|------|
| `dev` 分支 | `latest`, `sha-a1b2c3d` | 开发环境，自动更新 |
| `master` 分支 | `sha-e4f5g6h` | 生产环境，版本固定 |
| `v1.2.3` Tag | `1.2.3`, `sha-i7j8k9l` | 正式版本发布 |

### 查看流水线状态

1. 访问: https://github.com/shudelin111/nexusmall/actions
2. 筛选: "NexusMall CI/CD"
3. 查看 Member 相关的 Job

---

## ☸️ Kubernetes 部署

### 配置文件清单

Member 服务的 K8s 配置文件位于 `deploy/member-service/`:

```
deploy/member-service/
├── configmap.yaml        # 配置映射
├── deployment.yaml       # 部署配置
├── service.yaml          # 服务暴露
└── secret.yaml.example   # 密钥模板（需复制为 secret.yaml）
```

### 部署步骤

#### 1. 准备 Secret

```bash
cd deploy/member-service
cp secret.yaml.example secret.yaml

# 编辑 secret.yaml，填入真实的数据库密码等敏感信息
vim secret.yaml

# 应用 Secret
kubectl apply -f secret.yaml -n nexusmall
```

#### 2. 应用配置

```bash
# 应用所有配置
kubectl apply -f deploy/member-service/ -n nexusmall

# 或逐个应用
kubectl apply -f deploy/member-service/configmap.yaml -n nexusmall
kubectl apply -f deploy/member-service/deployment.yaml -n nexusmall
kubectl apply -f deploy/member-service/service.yaml -n nexusmall
```

#### 3. 验证部署

```bash
# 查看 Pod 状态
kubectl get pods -n nexusmall -l app=nexusmall-member

# 查看日志
kubectl logs -f deployment/nexusmall-member -n nexusmall

# 查看服务
kubectl get svc -n nexusmall -l app=nexusmall-member

# 健康检查
kubectl exec -it deployment/nexusmall-member -n nexusmall -- \
  curl http://localhost:10000/actuator/health
```

### 资源规格

| 资源类型 | Request | Limit |
|----------|---------|-------|
| CPU | 500m | 1000m |
| Memory | 2Gi | 4Gi |

### 扩缩容

```bash
# 手动扩容到 3 个副本
kubectl scale deployment nexusmall-member --replicas=3 -n nexusmall

# 查看 HPA（如果配置了自动扩缩容）
kubectl get hpa -n nexusmall
```

---

## 🔧 故障排查

### 问题 1: Docker 构建失败

**症状**: `mvn dependency:go-offline` 报错

**解决方案**:
```bash
# 清理 Maven 缓存
docker builder prune -a

# 重新构建（不使用缓存）
docker build --no-cache -t nexusmall-member:latest -f nexusmall-member/Dockerfile .
```

### 问题 2: CI/CD 流水线失败

**常见原因**:
1. **Maven 依赖下载失败**: 检查网络连接
2. **单元测试失败**: 查看 Artifacts 中的测试报告
3. **Docker Hub 认证失败**: 检查 Secrets 配置
4. **Git 推送冲突**: 等待其他模块推送完成（自动重试）

**解决方案**:
```bash
# 查看详细的流水线日志
# 在 GitHub Actions 页面点击失败的 Job → 查看详细日志
```

### 问题 3: Pod 启动失败

**诊断步骤**:
```bash
# 1. 查看 Pod 状态
kubectl describe pod <pod-name> -n nexusmall

# 2. 查看事件日志
kubectl get events -n nexusmall --sort-by='.lastTimestamp'

# 3. 查看容器日志
kubectl logs <pod-name> -n nexusmall --previous

# 4. 检查 ConfigMap 和 Secret
kubectl get configmap nexusmall-member -n nexusmall -o yaml
kubectl get secret nexusmall-member -n nexusmall -o yaml
```

### 问题 4: 健康检查失败

**可能原因**:
- 应用启动时间超过 `start-period` (60秒)
- Actuator 端点未正确配置
- 端口不匹配

**解决方案**:
```bash
# 临时增加启动时间
kubectl edit deployment nexusmall-member -n nexusmall
# 修改: start-period: 120s

# 检查 Actuator 配置
kubectl exec -it <pod-name> -n nexusmall -- \
  curl http://localhost:10000/actuator/health
```

### 问题 5: 内存溢出 (OOM)

**解决方案**:
```bash
# 1. 调整 JVM 参数（在 deployment.yaml 中）
env:
  - name: JAVA_OPTS
    value: "-XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

# 2. 增加 K8s 资源限制
resources:
  limits:
    memory: "6Gi"  # 从 4Gi 增加到 6Gi

# 3. 查看 GC 日志
kubectl cp <pod-name>:/tmp/logs/gc.log ./gc.log -n nexusmall
```

---

## 📊 监控与告警

### Prometheus 指标

Member 服务暴露以下指标端点：

- `/actuator/prometheus`: Prometheus 格式的指标
- `/actuator/metrics`: 所有可用指标列表

### 关键指标

| 指标名称 | 说明 | 告警阈值 |
|----------|------|----------|
| `jvm_memory_used_bytes` | JVM 内存使用量 | > 80% |
| `http_server_requests_seconds_count` | HTTP 请求数 | - |
| `http_server_requests_seconds_sum` | HTTP 请求总耗时 | - |
| `process_cpu_usage` | CPU 使用率 | > 70% |

---

## 🎯 最佳实践

### 1. 镜像版本管理

✅ **推荐**: 使用 SHA 标签确保版本可追溯  
❌ **避免**: 在生产环境使用 `latest` 标签

### 2. 资源限制

✅ **推荐**: 始终设置 requests 和 limits  
❌ **避免**: 不设置限制导致资源争抢

### 3. 健康检查

✅ **推荐**: 同时配置 liveness 和 readiness probe  
❌ **避免**: 只配置一种探针

### 4. 日志管理

✅ **推荐**: 使用结构化日志（JSON 格式）  
❌ **避免**: 打印大量 DEBUG 日志到生产环境

### 5. 安全加固

✅ **推荐**: 定期扫描镜像漏洞（Trivy）  
❌ **避免**: 使用有已知漏洞的基础镜像

---

## 📚 相关文档

- [Order 服务 Docker 指南](../nexusmall-order/README-Docker.md)
- [CI/CD 工作流配置](../../.github/workflows/ci-cd.yml)
- [Kubernetes 部署指南](../../docs/NACOS-CONFIG-DEPLOYMENT-GUIDE.md)
- [Nacos 配置中心](../../docs/nacos-configs/)

---

## 🆘 获取帮助

如有问题，请：

1. 查看 GitHub Issues: https://github.com/shudelin111/nexusmall/issues
2. 联系运维团队
3. 查阅内部 Wiki 文档

---

**最后更新**: 2026-04-06  
**维护者**: NexusMall Team
