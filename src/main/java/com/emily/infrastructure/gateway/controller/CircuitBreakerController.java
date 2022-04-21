package com.emily.infrastructure.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description :  断路器控制器
 * @Author :  Emily
 * @CreateDate :  Created in 2022/4/21 9:54 上午
 */
@RestController
@RequestMapping("circuitBreaker")
public class CircuitBreakerController {

    @GetMapping("fallback")
    public String fallback(){
        return "您的应用被老鹰叼走了，一会再来看吧";
    }
}
