##### 目标

- 配置Retry过滤器获取Reqeust参数

#### springcloud gateway网关

- 支持consul做为配置中心

- 支持consul作为注册中心

- 基于Springcloud gateway第二代网关开发

- 支持路径、请求头、响应头重写

- 支持限流

- 支持重试

- 支持跨域

- 支持权重分组

- 同时支持http、https及http自动跳转https

- 将springboot项目打包为docker镜像（https://www.runoob.com/docker/docker-build-command.html）

  ```properties
  # 清空并打包
  mvn clean package
  # docker build命令用于使用Dockerfile创建镜像
  # 语法：docker build [OPTIONS] PATH | URL | -
  # -f:指定要使用的Dockerfile路径
  # -t:镜像的名字及标签，通常是name:tag或name格式
  # .代表本次执行的上下文路径
  docker build -f ./Dockerfile . -t emilygateway:1.0.3
  # 运行构建的镜像
  docker run \
  -e JAVA_ACL_TOKEN=03259e78-848c-3ea8-c0f6-524279d52929 \
  -e JAVA_LOCAL_IP=127.0.0.1 \
  -e JAVA_LOCAL_PORT=80 \
  --restart=always \
  --privileged=true \
  -itd --name emilygateway \
  -p 80:80 \
  -p 443:443 \
  -p 7443:7443 \
  -v /Users/yaomingyang/Documents/logs:/app/logs \
  emilygateway:1.0.3
  ```
  
  登录Docker Registry
  
  ```properties
  sudo docker login --username=****@qq.com [Registry地址]
  ```
  
  将镜像推送到Registry
  
  ```properties
  sudo docker tag [ImageId] [Registry地址]/emilygateway:1.0.1
  sudo docker push [Registry地址]/emilygateway:1.0.1
  ```
  
  从Registry中拉取镜像
  
  ```properties
  sudo docker pull [Registry地址]/emilygateway:1.0.1
  ```
  
  