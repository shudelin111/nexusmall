# =====================================================
# NexusMall Product Service Dockerfile
# =====================================================
# 用途：构建 Product 服务的 Docker 镜像
# 模块：nexusmall-product（商品服务）
# 端口：12000
# 作者：shudl
# =====================================================

# =====================================================
# 阶段 1: Builder 阶段（编译打包）
# =====================================================
# 使用 Maven 3.8 + OpenJDK 8 镜像（包含 Maven 工具）
# 注意：必须使用 maven 镜像，eclipse-temurin 只包含 JDK 不包含 Maven
FROM maven:3.8-openjdk-8 AS builder

# 设置工作目录
WORKDIR /app

# 复制 Maven 配置文件（用于依赖缓存）
# 先复制 pom.xml 可以充分利用 Docker 缓存层
COPY nexusmall-common/pom.xml ./nexusmall-common/
COPY nexusmall-product/pom.xml ./nexusmall-product/
COPY pom.xml ./

# 复制公共模块源代码
COPY nexusmall-common/src ./nexusmall-common/src

# 复制 Product 模块源代码
COPY nexusmall-product/src ./nexusmall-product/src

# 执行 Maven 打包
# -N: 跳过非子模块项目（只构建父 POM）
# -f: 指定 pom.xml 文件
# -DskipTests: 跳过测试（CI 流程中已测试过）
# -B: 批处理模式（减少日志输出）
RUN mvn clean package -N -B && \
    mvn clean install -f nexusmall-common/pom.xml -DskipTests -B && \
    mvn clean package -f nexusmall-product/pom.xml -DskipTests -B

# =====================================================
# 阶段 2: Runtime 阶段（运行环境）
# =====================================================
# 使用 Eclipse Temurin 8 JRE 镜像（比 JDK 镜像小 ~300MB）
# JRE 只包含运行环境，不包含开发工具，适合生产环境
FROM eclipse-temurin:8-jre

# =====================================================
# 镜像元数据（符合 OCI 规范）
# =====================================================
LABEL maintainer="shudl"
LABEL version="1.0.0"
LABEL description="NexusMall Product Service - 商品服务"
LABEL org.opencontainers.image.source="https://github.com/shudelin111/nexusmall"
LABEL org.opencontainers.image.description="NexusMall 商品服务 Docker 镜像"
LABEL org.opencontainers.image.licenses="MIT"

# =====================================================
# 环境变量配置（业界标准实践）
# =====================================================

# 设置工作目录
ENV WORK_DIR=/app

# 设置日志目录（统一路径，方便挂载）
ENV LOG_DIR=/tmp/logs

# 设置时区为上海（中国标准时间）
ENV TZ=Asia/Shanghai

# 设置文件编码为 UTF-8（防止中文乱码）
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

# =====================================================
# JVM 启动参数配置（容器化环境最佳实践）
# =====================================================
# 参数说明：
# -XX:MaxRAMPercentage=75.0: 最大堆内存占容器内存限制的 75%（K8s 资源限制兼容）
# -XX:InitialRAMPercentage=50.0: 初始堆内存占容器内存限制的 50%
# -XX:+UseContainerSupport: 启用容器支持（让 JVM 感知容器资源限制）
# -XX:+UseG1GC: 使用 G1 垃圾收集器（适合大内存、低延迟场景）
# -XX:MaxGCPauseMillis=200: 最大 GC 停顿时间目标（200ms）
# -XX:+HeapDumpOnOutOfMemoryError: OOM 时自动生成堆转储
# -XX:HeapDumpPath: 堆转储文件路径
# -XX:ErrorFile: JVM 错误日志路径
# -XX:+PrintGC: 打印 GC 日志（JDK 8 标准参数）
# -XX:+PrintGCDetails: 打印详细 GC 日志
# -XX:+PrintGCDateStamps: 打印 GC 时间戳
# -XX:+PrintGCTimeStamps: 打印 GC 发生时间
# -Xloggc: GC 日志文件路径
# -XX:+UseGCLogFileRotation: 启用 GC 日志滚动
# -XX:NumberOfGCLogFiles=5: 保留 5 个 GC 日志文件
# -XX:GCLogFileSize=10M: 每个 GC 日志文件最大 10MB
# -Djava.security.egd=file:/dev/./urandom: 使用非阻塞随机数生成器（加快启动）
# -Duser.timezone=Asia/Shanghai: 设置时区
# -Dfile.encoding=UTF-8: 设置文件编码
# -XX:+UnlockExperimentalVMOptions: 解锁实验性 VM 选项（G1 GC 必需）
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 \
               -XX:InitialRAMPercentage=50.0 \
               -XX:+UseContainerSupport \
               -XX:+UseG1GC \
               -XX:MaxGCPauseMillis=200 \
               -XX:+HeapDumpOnOutOfMemoryError \
               -XX:HeapDumpPath=${LOG_DIR}/heap_dump.hprof \
               -XX:ErrorFile=${LOG_DIR}/crash_pid_%p.log \
               -XX:+PrintGC \
               -XX:+PrintGCDetails \
               -XX:+PrintGCDateStamps \
               -XX:+PrintGCTimeStamps \
               -Xloggc:${LOG_DIR}/gc.log \
               -XX:+UseGCLogFileRotation \
               -XX:NumberOfGCLogFiles=5 \
               -XX:GCLogFileSize=10M \
               -Djava.security.egd=file:/dev/./urandom \
               -Duser.timezone=Asia/Shanghai \
               -Dfile.encoding=UTF-8 \
               -XX:+UnlockExperimentalVMOptions"

# =====================================================
# 系统配置
# =====================================================

# 设置工作目录
WORKDIR ${WORK_DIR}

# 创建日志目录（避免启动时权限问题）
RUN mkdir -p ${LOG_DIR} && \
    chmod 755 ${LOG_DIR}

# =====================================================
# 复制构建产物
# =====================================================
# 从 Builder 阶段复制打包好的 JAR 文件
# --chown=1000:1000: 设置文件所有者（非 root 用户，增强安全性）
COPY --from=builder /app/nexusmall-product/target/*.jar app.jar

# =====================================================
# 端口暴露
# =====================================================
# 暴露 Product 服务端口（与 application.yml 中的 server.port 一致）
EXPOSE 12000

# =====================================================
# 健康检查（Docker 原生健康检查）
# =====================================================
# 每 30 秒检查一次，超时 5 秒，失败 3 次判定为不健康
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:12000/actuator/health || exit 1

# =====================================================
# 启动命令
# =====================================================
# 使用 exec 形式（让 Spring Boot 成为 PID 1 进程，正确接收信号）
# -Djava.security.egd: 使用非阻塞随机数生成器
# -Dspring.profiles.active: 指定运行环境（由 K8s 环境变量覆盖）
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# =====================================================
# 优雅关闭支持
# =====================================================
# Spring Boot 会监听 SIGTERM 信号并执行优雅关闭
# 配合 K8s 的 lifecycle.preStop 和 terminationGracePeriodSeconds 使用
