springboot2.4开启HTTPS功能报DerInputStream.getLength(): lengthTag=111, too big异常

##### 1.生成证书

```properties
keytool -genkey -alias emily -keypass 123456 -keyalg RSA -keysize 1024 -validity 3650 -keystore D:\emily.p12 -deststoretype pkcs12 -storepass 123456
```

##### 2.将emily.p12证书放到项目resources目录下，在yml配置文件中添加配置

```yml
server:
  port: 443
  # 优雅停机
  shutdown: graceful
  ssl:
    # 是否启用SSL支持
    enabled: true
    # 标识秘钥存储中秘钥的别名
    key-alias: emily
    # 访问秘钥存储的密码
    key-store-password: 123456
    # 指定保存SSL证书的秘钥存储的路径（通常是jks文件）
    key-store: classpath:emily.p12
    # 秘钥存储的类型
    key-store-type: PKCS12
    # 要使用的SSL协议
    protocol: TLS
```

##### 3.上述配置好之后按理启动服务就可以开启https能力，但是不幸报错了

```java
Caused by: java.io.IOException: DerInputStream.getLength(): lengthTag=111, too big.
	at sun.security.util.DerInputStream.getLength(DerInputStream.java:599) ~[na:1.8.0_181]
	at sun.security.util.DerValue.init(DerValue.java:391) ~[na:1.8.0_181]
	at sun.security.util.DerValue.<init>(DerValue.java:332) ~[na:1.8.0_181]
	at sun.security.util.DerValue.<init>(DerValue.java:345) ~[na:1.8.0_181]
	at sun.security.pkcs12.PKCS12KeyStore.engineLoad(PKCS12KeyStore.java:1938) ~[na:1.8.0_181]
	at java.security.KeyStore.load(KeyStore.java:1445) ~[na:1.8.0_181]
	at org.springframework.boot.web.embedded.netty.SslServerCustomizer.loadStore(SslServerCustomizer.java:173) ~[spring-boot-2.4.1.jar:2.4.1]
	... 21 common frames omitted
```

> 看到上面的错误有点懵逼，秘钥生成的没问题的，之前已经测试过了，然后就开始度娘，最后添加一个maven插件解决；

```xml
            <plugin>
                <artifactId>maven-resources-plugin</artifactId>
                <version>3.2.0</version>
                <configuration>
                    <encoding>utf-8</encoding>
                    <!-- 解决Spring boot引起的profile失效问题 -->
                    <useDefaultDelimiters>true</useDefaultDelimiters>
                    <!-- 过滤后缀为p12、pem、pfx的证书文件 -->
                    <nonFilteredFileExtensions>
                        <nonFilteredFileExtension>p12</nonFilteredFileExtension>
                        <nonFilteredFileExtension>cer</nonFilteredFileExtension>
                        <nonFilteredFileExtension>pem</nonFilteredFileExtension>
                        <nonFilteredFileExtension>pfx</nonFilteredFileExtension>
                        <nonFilteredFileExtension>jkx</nonFilteredFileExtension>
                    </nonFilteredFileExtensions>
                </configuration>
            </plugin>
```

这个插件的作用是在maven编译打包项目的时候忽略指定后缀的文件，秘钥如果不忽略就会被编译，编译后就会出问题；

GitHub地址：[https://github.com/mingyang66/EmilyGateway](https://github.com/mingyang66/EmilyGateway)

