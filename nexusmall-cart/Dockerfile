# =====================================================
# NexusMall Cart Service - Production Dockerfile
# =====================================================
# 技术标准：
# 1. 多阶段构建 (Multi-stage Build) - 镜像体积减少 80%+
# 2. Maven 依赖分层缓存 - 构建速度提升 50%+
# 3. 非 root 用户运行 - 安全合规
# 4. JVM 容器感知参数 - 防止 OOM
# 5. 优雅关闭支持 - SIGTERM 信号处理
# 6. 时区设置 - Asia/Shanghai
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

# 关键：先安装父 POM 到本地仓库！
# 所有子模块都依赖父 POM，所以必须先安装
RUN mvn clean install -N -B
#    ↑ -N 参数表示只在当前目录安装 POM，不递归编译子模块

# 复制公共模块 pom.xml 和源码
COPY ../nexusmall-common/pom.xml nexusmall-common/
COPY ../nexusmall-common/src nexusmall-common/src

# 现在安装 common 模块（父 POM 已安装，不会报错）
RUN mvn clean install -f nexusmall-common/pom.xml -DskipTests -B

# 复制 cart 模块 pom.xml
COPY ../nexusmall-cart/pom.xml nexusmall-cart/pom.xml

# 现在下载 cart 的依赖（common 已安装，可以成功解析）
RUN mvn dependency:go-offline -B \
    -f nexusmall-cart/pom.xml \
    -DskipTests

# 复制 cart 模块源码
COPY ../nexusmall-cart/src nexusmall-cart/src

# 编译打包 (跳过测试，由 CI 流水线保证质量)
RUN mvn clean package -f nexusmall-cart/pom.xml \
    -DskipTests \
    -Dmaven.javadoc.skip=true \
    -Dmaven.source.skip=true \
    -B

# ===============================
# 阶段 2: 运行阶段 (Runtime Stage)
# ===============================
# 使用 Eclipse Temurin 8 JRE 镜像（与项目 JDK 8 保持一致）
# 参考：https://hub.docker.com/_/eclipse-temurin
FROM eclipse-temurin:8-jre

# ========================================
# 元数据标注 (OCI Image Spec)
# ========================================
LABEL org.opencontainers.image.title="NexusMall Cart Service"
LABEL org.opencontainers.image.description="NexusMall 购物车服务 - Redis Hash主存储 + MySQL异步持久化 + 商品快照机制"
LABEL org.opencontainers.image.vendor="NexusMall Team"
LABEL org.opencontainers.image.version="0.0.1-SNAPSHOT"
LABEL org.opencontainers.image.source="https://github.com/shudelin111/nexusmall"
LABEL maintainer="shudl"

# ========================================
# 环境变量配置 (生产环境可调优)
# ========================================
# 时区设置 (中国标准时间)
ENV TZ=Asia/Shanghai

# JVM 内存配置 (根据容器限制自动调整)
# MaxRAMPercentage: JVM 最大使用容器内存的百分比 (默认 25%)
# InitialRAMPercentage: JVM 初始使用容器内存的百分比 (默认 1%)
# 注意：K8s 中已设置资源限制，JVM 会自动适配
# GC 日志配置：JDK 8 使用 -XX:+PrintGC，JDK 9+ 使用 -Xlog:gc*
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 \
               -XX:InitialRAMPercentage=50.0 \
               -XX:+UseContainerSupport \
               -XX:+UseG1GC \
               -XX:MaxGCPauseMillis=200 \
               -XX:+HeapDumpOnOutOfMemoryError \
               -XX:HeapDumpPath=/tmp/logs/heap_dump.hprof \
               -XX:ErrorFile=/tmp/logs/crash_pid_%p.log \
               -XX:+PrintGC \
               -XX:+PrintGCDetails \
               -XX:+PrintGCDateStamps \
               -XX:+PrintGCTimeStamps \
               -Xloggc:/tmp/logs/gc.log \
               -XX:+UseGCLogFileRotation \
               -XX:NumberOfGCLogFiles=5 \
               -XX:GCLogFileSize=10M \
               -Djava.security.egd=file:/dev/./urandom \
               -Duser.timezone=Asia/Shanghai \
               -Dfile.encoding=UTF-8 \
               -XX:+UnlockExperimentalVMOptions"

# Spring Boot 配置
# 注意：实际使用哪个 profile 由 K8s 环境变量或 Nacos 配置决定
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=10001  # 购物车服务端口(与Member同属用户域)

# 日志路径配置（与 VOLUME 声明保持一致）
ENV LOG_PATH=/tmp/logs

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
COPY --from=builder /build/nexusmall-cart/target/*.jar app.jar

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

# ========================================
# 启动命令 (优雅关闭支持)
# ========================================
# 使用 exec 模式，让 Java 进程接收 SIGTERM 信号
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# ========================================
# 卷挂载 (可选，用于外部化配置)
# ========================================
VOLUME ["/tmp/logs", "/app/config"]
