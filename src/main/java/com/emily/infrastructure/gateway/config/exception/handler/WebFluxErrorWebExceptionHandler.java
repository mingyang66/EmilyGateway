package com.emily.infrastructure.gateway.config.exception.handler;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.*;

import java.util.Map;
import java.util.Objects;


/**
 * @author Emily
 */
public class WebFluxErrorWebExceptionHandler extends DefaultErrorWebExceptionHandler {

    public WebFluxErrorWebExceptionHandler(ErrorAttributes errorAttributes, WebProperties.Resources resourceProperties,
                                           ErrorProperties errorProperties, ApplicationContext applicationContext) {
        super(errorAttributes, resourceProperties, errorProperties, applicationContext);
    }

    /**
     * 指定响应处理方法为JSON处理的方法
     *
     * @param errorAttributes
     */
    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    /**
     * 根据code获取对应的HttpStatus
     *
     * @param errorAttributes
     * @return
     */
    @Override
    public int getHttpStatus(Map<String, Object> errorAttributes) {
        int statusCode = (int) errorAttributes.get("status");
        return statusCode;
    }

    @Override
    public ErrorAttributeOptions getErrorAttributeOptions(ServerRequest request, MediaType mediaType) {
        return ErrorAttributeOptions.of(
                ErrorAttributeOptions.Include.EXCEPTION,
                ErrorAttributeOptions.Include.STACK_TRACE,
                ErrorAttributeOptions.Include.MESSAGE,
                ErrorAttributeOptions.Include.BINDING_ERRORS
        );
    }

    @Override
    protected Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> attributes = super.getErrorAttributes(request, options);
        Map<String, Object> responseMap = Maps.newLinkedHashMap();
        responseMap.put("status", attributes.get("status"));
        responseMap.put("message", Objects.nonNull(attributes.get("error")) ? attributes.get("error") : attributes.get("message"));
        return responseMap;
    }
}