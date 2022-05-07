

## Emily网关服务

#### 项目依赖版本

- JDK1.8
- springboot2.6.7
- 基础库组件4.0.10
- springcloud3.1.0
- springcloud gateway3.1.2
- Resilience4j2.1.2

#### 支持功能

- 支持同时启动两个端口，一个http端口一个https端口，支持由http跳转到https；
- 支持全局捕获网关异常，打印日志到指定的日志文件，方便查问题；
- 支持基于Resilience4j的断路器熔断支持；
- 支持跨域全局设置；
- 支持对网关http|https对下游请求相关参数设置，如：连接超时时间等；

#### 网关配置示例

请参考项目目录下的application系列配置文件