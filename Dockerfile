# 定制镜像所需的基础镜像
# FROM openjdk:8-jdk-alpine
FROM adoptopenjdk/openjdk11:x86_64-alpine-jre-11.0.9_11
# 用于执行后面跟着的命令行命令
RUN echo 'JDK11 Images Download Success'
# 作者
MAINTAINER Emily
# 工作目录路径
WORKDIR /app
# 构建参数
ARG JAR_FILE=target/*.jar
# 复制指令，从上下文目录中复制文件或目录到容器里指定路径
COPY ${JAR_FILE} emilygateway.jar
# 运行程序指令 @link{reactor.netty.resources.ConnectionProvider}
ENTRYPOINT ["java", "-Dreactor.netty.pool.leasingStrategy=lifo","-Dreactor.netty.http.server.accessLogEnabled=true","-jar","emilygateway.jar"]