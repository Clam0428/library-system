# 使用官方的 OpenJDK 8 作为基础镜像
FROM openjdk:8-jdk-alpine

# 维护者信息
MAINTAINER YourName <your@email.com>

# 设置工作目录
WORKDIR /app

# 复制打包好的 JAR 文件到容器中
COPY target/library-system-2.0.0.jar app.jar

# 暴露端口（Spring Boot 默认端口）
EXPOSE 8888

# 设置环境变量（可通过外部环境覆盖）
ENV DB_USERNAME=root
ENV DB_PASSWORD=root
ENV DB_HOST=localhost
ENV DB_PORT=3306
ENV DB_NAME=library_db
ENV REDIS_HOST=localhost
ENV REDIS_PORT=6379

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]
