package com.emily.infrastructure.gateway.common;

import com.emily.infrastructure.gateway.common.entity.BaseLogger;
import com.emily.infrastructure.json.JsonUtils;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Map;

/**
 * @Description :  记录日志
 * @Author :  Emily
 * @CreateDate :  Created in 2022/7/5 7:58 下午
 */
public class RecordLogger {
    private static final Logger logger = LoggerFactory.getLogger("userActionLogger");

    public static void recordUser(ServerRequest request, Map<String, Object> attributes, String status, String message){
        BaseLogger userAction = new BaseLogger();
        userAction.setUrl(request.path());
        userAction.setResponseBody(attributes);
        Map<String, Object> inParams = Maps.newHashMap();
        inParams.put("headers", request.headers().asHttpHeaders());
        userAction.setRequestParams(inParams);
        logger.info(JsonUtils.toJSONString(userAction));
    }
}
