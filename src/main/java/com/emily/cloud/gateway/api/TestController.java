package com.emily.cloud.gateway.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @program: spring-cloud-gateway
 * @description:
 * @create: 2020/12/18
 */
@RestController
@RequestMapping("gateway")
public class TestController {


    @GetMapping("quickly")
    public String gateway(){
        return "Gateway is quickly";
    }

    private static AtomicInteger position = new AtomicInteger(0);;
    public static void main(String[] args) {
        System.out.println(position.incrementAndGet());
        System.out.println(position.incrementAndGet());
    }
}
