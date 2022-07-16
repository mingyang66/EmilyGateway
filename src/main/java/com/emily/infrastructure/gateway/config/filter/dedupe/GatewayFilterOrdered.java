package com.emily.infrastructure.gateway.config.filter.dedupe;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.core.Ordered;

/**
 * @Description :  可排序过滤器
 * @Author :  Emily
 * @CreateDate :  Created in 2022/7/11 11:02 上午
 */
public interface GatewayFilterOrdered extends GatewayFilter, Ordered {
}
