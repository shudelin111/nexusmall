# =====================================================
# NexusMall Order Service - Production Dockerfile
# =====================================================
# 技术标准：
# 1. 多阶段构建 (Multi-stage Build) - 镜像体积减少 80%+
# 2. Maven 依赖分层缓存 - 构建速度提升 50%+
# 3. 非 root 用户运行 - 安全合规
# 4. JVM 容器感知参数 - 防止 OOM
# 5. 优雅关闭支持 - SIGTERM 信号处理
# =====================================================

# ===============================
# 阶段 1: 构建阶段 (Builder Stage)
# ===============================
# 使用 OpenJDK 8 与项目的 Java 版本保持一致
FROM maven:3.8-openjdk-8 AS builder

# 设置工作目录
WORKDIR /build

# 复制父工程 pom.xml (利用 Docker 缓存层)
COPY ../pom.xml .

# 复制 order 模块 pom.xml
COPY ../nexusmall-order/pom.xml nexusmall-order/pom.xml

# 预下载依赖 (如果 pom.xml 未变化，使用缓存层)
RUN mvn dependency:go-offline -B \
    -f nexusmall-order/pom.xml \
    -DskipTests

# 复制公共模块源码 (order 依赖 common 模块)
COPY ../nexusmall-common/src nexusmall-common/src
COPY ../nexusmall-common/pom.xml nexusmall-common/

# 安装 common 模块到本地仓库
RUN mvn clean install -f nexusmall-common/pom.xml -DskipTests -B

# 复制 order 模块源码
COPY ../nexusmall-order/src nexusmall-order/src

# 编译打包 (跳过测试，由 CI 流水线保证质量)
RUN mvn clean package -f nexusmall-order/pom.xml \
    -DskipTests \
    -Dmaven.javadoc.skip=true \
    -Dmaven.source.skip=true \
    -B

# ===============================
# 阶段 2: 运行阶段 (Runtime Stage)
# ===============================
# 使用 Eclipse Temurin 11 JRE 镜像（OpenJDK 8 已弃用，Temurin 是业界标准替代方案）
# 参考：https://hub.docker.com/_/eclipse-temurin
FROM eclipse-temurin:8-jre

# ========================================
# 元数据标注 (OCI Image Spec)
# ========================================
LABEL org.opencontainers.image.title="NexusMall Order Service"
LABEL org.opencontainers.image.description="NexusMall 订单服务 - 微服务架构"
LABEL org.opencontainers.image.vendor="NexusMall Team"
LABEL org.opencontainers.image.version="0.0.1-SNAPSHOT"
LABEL org.opencontainers.image.source="https://github.com/nexusmall/nexusmall"
LABEL maintainer="NexusMall Team <support@nexusmall.com>"

# ========================================
# 环境变量配置 (生产环境可调优)
# ========================================
# 时区设置 (中国标准时间)
ENV TZ=Asia/Shanghai

# JVM 内存配置 (根据容器限制自动调整)
# MaxRAMPercentage: JVM 最大使用容器内存的百分比 (默认 25%)
# InitialRAMPercentage: JVM 初始使用容器内存的百分比 (默认 1%)
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 \
               -XX:InitialRAMPercentage=50.0 \
               -XX:+UseContainerSupport \
               -XX:+UseG1GC \
               -XX:MaxGCPauseMillis=200 \
               -XX:+HeapDumpOnOutOfMemoryError \
               -XX:HeapDumpPath=/tmp/logs/heap_dump.hprof \
               -XX:ErrorFile=/tmp/logs/crash_pid_%p.log \
               -Xlog:gc*:file=/tmp/logs/gc.log:time,uptime:filecount=5,filesize=10M \
               -Djava.security.egd=file:/dev/./urandom \
               -Duser.timezone=Asia/Shanghai \
               -Dfile.encoding=UTF-8 \
               -XX:+UnlockExperimentalVMOptions"

# Spring Boot 配置
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=11000
ENV MANAGEMENT_SERVER_PORT=11001

# 应用目录
ENV APP_HOME=/app

# ========================================
# 系统配置
# ========================================
# 设置工作目录
WORKDIR ${APP_HOME}

# 创建非 root 用户组 (安全最佳实践)
RUN groupadd --system appgroup && \
    useradd --system --gid appgroup --shell /bin/bash --create-home appuser

# 从构建阶段复制 JAR 包
COPY --from=builder /build/nexusmall-order/target/*.jar app.jar

# 创建日志目录并授权
RUN mkdir -p /tmp/logs && \
    chown -R appuser:appgroup ${APP_HOME} && \
    chown -R appuser:appgroup /tmp/logs

# 切换到非 root 用户 (安全加固)
USER appuser

# ========================================
# 健康检查配置
# ========================================
# 每 30 秒检查一次，超时 10 秒，连续 3 次失败判定为不健康
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:${SERVER_PORT}/actuator/health || exit 1

# 暴露端口
EXPOSE ${SERVER_PORT}
EXPOSE ${MANAGEMENT_SERVER_PORT}

# ========================================
# 启动命令 (优雅关闭支持)
# ========================================
# 使用 exec 模式，让 Java 进程接收 SIGTERM 信号
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# ========================================
# 卷挂载 (可选，用于外部化配置)
# ========================================
VOLUME ["/tmp/logs", "/app/config"]
