package com.emily.cloud.gateway.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        return "quickly";
    }

}
