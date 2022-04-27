package com.emily.infrastructure.gateway.route;

import reactor.core.publisher.Mono;

/**
 * @Description :
 * @Author :  Emily
 * @CreateDate :  Created in 2022/4/25 4:01 下午
 */
public class Test {
    public static void main(String[] args) {
       Mono.just(23/0)
               .doOnSuccess(integer -> System.out.println(integer))
               .doOnError(throwable -> System.out.println(throwable))
               .onErrorResume(throwable -> Mono.error(throwable));
    }
}
