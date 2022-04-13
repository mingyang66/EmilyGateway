package com.emily.infrastructure.gateway.predicate;

import java.util.function.Predicate;

/**
 * @program: EmilyGateway
 * @description: 自定义断言
 * @create: 2021/01/23
 */
public class RequestBodyPredicate implements Predicate {
    /**
     * 根据内容判定路由是否匹配
     * @param o
     * @return
     */
    @Override
    public boolean test(Object o) {
        return true;
    }
}
