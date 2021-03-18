package com.emily.cloud.gateway.route;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.data.redis.core.RedisTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @program: EmilyGateway
 * @description: 网关路由定义仓储
 * @create: 2021/03/16
 */
public class RedisRouteDefinitionRepository implements RouteDefinitionRepository {

    public static final String GATEWAY_ROUTE_KEY = "GATEWAY_ROUTE_KEY";

    private RedisTemplate redisTemplate;

    public RedisRouteDefinitionRepository(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return route.flatMap(r -> {
            if (StringUtils.isEmpty(r.getId())) {
                return Mono.error(new IllegalArgumentException("id may not be empty"));
            }
            redisTemplate.opsForHash().put(GATEWAY_ROUTE_KEY, r.getId(), r);
            return Mono.empty();
        });
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeId.flatMap(id -> {
            if (redisTemplate.opsForHash().hasKey(GATEWAY_ROUTE_KEY, id)) {
                redisTemplate.opsForHash().delete(GATEWAY_ROUTE_KEY, id);
                return Mono.empty();
            }
            return Mono.defer(() -> Mono.error(new NotFoundException("RouteDefinition not found: " + routeId)));
        });
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        return Flux.fromIterable(redisTemplate.opsForHash().values(GATEWAY_ROUTE_KEY));
    }
}
