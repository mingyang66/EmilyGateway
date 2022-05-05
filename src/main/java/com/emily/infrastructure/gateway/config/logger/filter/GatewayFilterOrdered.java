package com.emily.infrastructure.gateway.config.logger.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.core.Ordered;

/**
 * @Description : 自定义局部过滤器，允许定义顺序
 * @Author :  Emily
 * @CreateDate :  Created in 2022/4/28 6:14 下午
 */
public abstract class GatewayFilterOrdered implements GatewayFilter, Ordered {
}
