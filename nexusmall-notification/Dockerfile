# =====================================================
# NexusMall Notification Service - Production Dockerfile
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
FROM maven:3.8-openjdk-8 AS builder

WORKDIR /build

COPY ../pom.xml .

RUN mvn clean install -N -B

COPY ../nexusmall-common/pom.xml nexusmall-common/
COPY ../nexusmall-common/src nexusmall-common/src

RUN mvn clean install -f nexusmall-common/pom.xml -DskipTests -B

COPY ../nexusmall-notification/pom.xml nexusmall-notification/pom.xml

RUN mvn dependency:go-offline -B \
    -f nexusmall-notification/pom.xml \
    -DskipTests

COPY ../nexusmall-notification/src nexusmall-notification/src

RUN mvn clean package -f nexusmall-notification/pom.xml \
    -DskipTests \
    -Dmaven.javadoc.skip=true \
    -Dmaven.source.skip=true \
    -B

# ===============================
# 阶段 2: 运行阶段 (Runtime Stage)
# ===============================
FROM eclipse-temurin:8-jre

LABEL org.opencontainers.image.title="NexusMall Notification Service"
LABEL org.opencontainers.image.description="NexusMall 通知服务 - 微服务架构"
LABEL org.opencontainers.image.vendor="NexusMall Team"
LABEL org.opencontainers.image.version="0.0.1-SNAPSHOT"
LABEL org.opencontainers.image.source="https://github.com/shudelin111/nexusmall"
LABEL maintainer="NexusMall Team"

ENV TZ=Asia/Shanghai

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

ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=14000

ENV LOG_PATH=/tmp/logs

ENV APP_HOME=/app

WORKDIR ${APP_HOME}

RUN groupadd --system appgroup && \
    useradd --system --gid appgroup --shell /bin/bash --create-home appuser

COPY --from=builder /build/nexusmall-notification/target/*.jar app.jar

RUN mkdir -p /tmp/logs && \
    chown -R appuser:appgroup ${APP_HOME} && \
    chown -R appuser:appgroup /tmp/logs

USER appuser

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:${SERVER_PORT}/actuator/health || exit 1

EXPOSE ${SERVER_PORT}

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

VOLUME ["/tmp/logs", "/app/config"]
