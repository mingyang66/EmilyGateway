package com.emily.infrastructure.gateway.route;

import reactor.core.publisher.Mono;

/**
 * @Description :
 * @Author :  Emily
 * @CreateDate :  Created in 2022/4/25 4:01 下午
 */
public class Test {
    public static void main(String[] args) {
        Mono.just(1/0).onErrorResume(throwable -> {
            System.out.println(throwable.getMessage());
            return Mono.error(throwable);
        });
    }
}
