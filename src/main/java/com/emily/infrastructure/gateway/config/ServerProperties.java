package com.emily.infrastructure.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.server.Shutdown;

/**
 * @program: EmilyGateway
 * @description: Netty服务器配置
 * @create: 2021/01/13
 */
@ConfigurationProperties(prefix = "server.http")
public class ServerProperties {
    /**
     * 是否开启http端口号
     */
    private boolean enable;
    /**
     * http端口号
     */
    private int port = 8080;
    /**
     * 关闭模式
     */
    private Shutdown shutdown = Shutdown.IMMEDIATE;
    /**
     * http请求是否自动跳转到https
     */
    private boolean httpToHttps = true;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Shutdown getShutdown() {
        return shutdown;
    }

    public void setShutdown(Shutdown shutdown) {
        this.shutdown = shutdown;
    }

    public boolean isHttpToHttps() {
        return httpToHttps;
    }

    public void setHttpToHttps(boolean httpToHttps) {
        this.httpToHttps = httpToHttps;
    }
}
