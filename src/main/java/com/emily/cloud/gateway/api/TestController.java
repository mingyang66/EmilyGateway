package com.emily.cloud.gateway.api;

import io.micrometer.core.annotation.Timed;
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
    @Timed(value = "asfd", description = "afd")
    public String gateway(){

        return "Gateway is quickly";
    }

    public static void main(String[] args) {
        SleuthTraceController sleuthTraceController = new SleuthTraceController();
        SleuthTraceController sleuthTraceController1 = new SleuthTraceController();
        System.out.println(getIdentityHexString(sleuthTraceController));
        System.out.println(getIdentityHexString(sleuthTraceController));
        System.out.println(getIdentityHexString(sleuthTraceController));
        System.out.println(getIdentityHexString(sleuthTraceController1));
        System.out.println(getIdentityHexString(sleuthTraceController1));

    }
    public static String getIdentityHexString(Object obj) {
        return Integer.toHexString(System.identityHashCode(obj));
    }
}
