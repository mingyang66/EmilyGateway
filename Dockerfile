FROM openjdk:8-jdk-alpine
#VOLUME /tmp
WORKDIR /app
# ARG即pom文件中指定的参数 -- buildArgs
ARG JAR_FILE
ADD ${JAR_FILE} emilygateway.jar
ENTRYPOINT ["java","-jar","emilygateway.jar"]