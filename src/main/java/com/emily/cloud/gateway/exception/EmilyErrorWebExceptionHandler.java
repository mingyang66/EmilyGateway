package com.emily.cloud.gateway.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * @program: EmilyGateway
 * @description: 自定义异常处理
 * @create: 2021/01/06
 */
public class EmilyErrorWebExceptionHandler extends DefaultErrorWebExceptionHandler {

    private static Logger logger = LoggerFactory.getLogger(EmilyErrorWebExceptionHandler.class);

    public EmilyErrorWebExceptionHandler(ErrorAttributes errorAttributes, WebProperties.Resources resources, ErrorProperties errorProperties, ApplicationContext applicationContext) {
        super(errorAttributes, resources, errorProperties, applicationContext);
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

  /*  @Override
    protected Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(request, options);
        Map<String, Object> errorData = new LinkedHashMap<>();
        errorData.put("status", errorAttributes.get("status"));
        errorData.put("message", StringUtils.join(errorAttributes.get("error")));
        logger.error(errorAttributes.toString());
        return errorData;
    }*/
}
