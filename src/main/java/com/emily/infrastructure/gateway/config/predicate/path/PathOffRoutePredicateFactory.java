package com.emily.infrastructure.gateway.config.predicate.path;

import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.GatewayPredicate;
import org.springframework.util.AntPathMatcher;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * @Description :  路由关闭断言
 * @Author :  姚明洋
 * @CreateDate :  Created in 2022/7/7 10:30 上午
 */
public class PathOffRoutePredicateFactory extends AbstractRoutePredicateFactory<PathOffRoutePredicateFactory.Config> {

    private static final String MATCH_TRAILING_SLASH = "matchTrailingSlash";

    private AntPathMatcher matcher = new AntPathMatcher();

    public PathOffRoutePredicateFactory() {
        super(Config.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("patterns", MATCH_TRAILING_SLASH);
    }

    @Override
    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST_TAIL_FLAG;
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        final ArrayList<String> pathPatterns = new ArrayList<>();
        config.getPatterns().forEach(pattern -> {
            pathPatterns.add(pattern);
        });

        return (GatewayPredicate) exchange -> {
            String path = exchange.getRequest().getURI().getRawPath();
            boolean match = false;
            for (int i = 0; i < pathPatterns.size(); i++) {
                if (matcher.match(pathPatterns.get(i), path)) {
                    match = true;
                    break;
                }
            }
            if (match) {
                return false;
            }
            return true;
        };
    }

    @Validated
    public static class Config {

        private List<String> patterns = new ArrayList<>();

        private boolean matchTrailingSlash = true;

        public List<String> getPatterns() {
            return patterns;
        }

        public Config setPatterns(List<String> patterns) {
            this.patterns = patterns;
            return this;
        }

        public boolean isMatchTrailingSlash() {
            return matchTrailingSlash;
        }

        public void setMatchTrailingSlash(boolean matchTrailingSlash) {
            this.matchTrailingSlash = matchTrailingSlash;
        }
    }
}
