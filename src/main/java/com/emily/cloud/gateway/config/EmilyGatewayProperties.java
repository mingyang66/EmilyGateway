package com.emily.cloud.gateway.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @program: EmilyGateway
 * @description: 网关配置类
 * @create: 2021/03/03
 */
@SuppressWarnings("all")
@ConfigurationProperties(prefix = "spring.emily.gateway")
public class EmilyGatewayProperties {
    /**
     * 网关路由访问日志记录限制
     */
    private List<Route> excludeLoggingRoutes = new ArrayList<>();
    /**
     * 外部网络访问限制
     */
    private List<External> excludeExternelRoutes = new ArrayList<>();


    /**
     * 获取指定路由 ID对应的内部网络限制配置
     *
     * @param id 路由ID
     */
    public External getExcludeExternelRoute(String id) {
        if (StringUtils.isEmpty(id)) {
            return null;
        }
        for (int i = 0; i < excludeExternelRoutes.size(); i++) {
            if (StringUtils.equals(excludeExternelRoutes.get(i).getId(), id)) {
                return excludeExternelRoutes.get(i);
            }
        }
        return null;
    }

    public List<External> getExcludeExternelRoutes() {
        return excludeExternelRoutes;
    }

    public void setExcludeExternelRoutes(List<External> excludeExternelRoutes) {
        this.excludeExternelRoutes = excludeExternelRoutes;
    }

    public List<Route> getExcludeLoggingRoutes() {
        return excludeLoggingRoutes;
    }

    /**
     * 获取指定路由配置的路由信息
     *
     * @param id 路由ID
     * @return 路由配置信息
     */
    public Route getExcludeLoggingRoute(String id) {
        if (StringUtils.isEmpty(id)) {
            return null;
        }
        for (int i = 0; i < excludeLoggingRoutes.size(); i++) {
            if (StringUtils.equals(excludeLoggingRoutes.get(i).getId(), id)) {
                return excludeLoggingRoutes.get(i);
            }
        }
        return null;
    }

    public void setExcludeLoggingRoutes(List<Route> excludeLoggingRoutes) {
        this.excludeLoggingRoutes = excludeLoggingRoutes;
    }

    /**
     * 网关路由访问日志记录限制
     */
    public static class Route {
        /**
         * 网关路由配置ID
         */
        private String id;
        /**
         * 路由
         */
        private Set<String> path = new HashSet<>();

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Set<String> getPath() {
            return path;
        }

        public void setPath(Set<String> path) {
            this.path = path;
        }
    }

    /**
     * 外部网络访问限制
     */
    public static class External {
        /**
         * 网关路由配置ID
         */
        private String id;
        /**
         * 路由
         */
        private Set<String> path = new HashSet<>();

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Set<String> getPath() {
            return path;
        }

        public void setPath(Set<String> path) {
            this.path = path;
        }
    }
}
