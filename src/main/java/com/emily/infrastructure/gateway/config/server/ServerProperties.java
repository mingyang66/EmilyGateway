package com.emily.infrastructure.gateway.config.server;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.server.Shutdown;

/**
 * @author Emily
 * @program: EmilyGateway
 * @description: Netty服务器配置
 * @create: 2021/01/13
 */
@ConfigurationProperties(prefix = ServerProperties.PREFIX)
public class ServerProperties {
    /**
     * 默认前缀
     */
    public static final String PREFIX = "server.http";
    /**
     * 是否开启http端口号
     */
    private boolean enabled;
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
