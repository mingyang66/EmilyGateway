  # 端口号占用查询
  # lsof -i tcp:port
  echo '开始打包...'
  mvn clean package
  echo '打包完成...'
  # docker build命令用于使用Dockerfile创建镜像
  # 语法：docker build [OPTIONS] PATH | URL | -
  # -f:指定要使用的Dockerfile路径
  # -t:镜像的名字及标签，通常是name:tag或name格式
  # .代表本次执行的上下文路径
  docker build -f ./Dockerfile . -t emilygateway:1.0.3
  echo '镜像构建完成...'
  # 运行构建的镜像
  docker run \
  -e JAVA_ACL_TOKEN=03259e78-848c-3ea8-c0f6-524279d52929 \
  -e JAVA_LOCAL_IP=172.30.67.122 \
  -e JAVA_LOCAL_PORT=80 \
  --restart=always \
  --privileged=true \
  -itd --name emilygateway \
  -p 80:80 \
  -p 443:443 \
  -p 7443:7443 \
  -v /Users/yaomingyang/Documents/logs:/app/logs \
  emilygateway:1.0.3
  echo '容器创建成功...'