# EmilyGateway
springcloud网关

- 支持consul做为配置中心

- 支持consul作为注册中心

- 基于Springcloud gateway第二代网关开发

- 支持路径、请求头、响应头重写

- 支持限流

- 支持重试

- 支持跨域

- 支持权重分组

- 同时支持http、https及http自动跳转https

  ```properties
   docker install dockerfile:build
   docker run -d -p 80:80 -p 443:443 emilygateway 
  
  docker run \
  -e JAVA_ACL_TOKEN=03259e78-848c-3ea8-c0f6-524279d52929 \
  -e JAVA_LOCAL_IP=172.30.67.122 \
  -e JAVA_LOCAL_PORT=80 \
  --restart=always \
  --privileged=true \
  -itd --name emilygateway \
  -p 80:80 \
  -p 443:443 \
  -p 7743:7743 \
  -v /Users/yaomingyang/Documents/logs:/app/logs \
  emilygateway
  ```

  