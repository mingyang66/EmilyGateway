package com.emily.cloud.gateway.exception;

import com.emily.framework.common.enums.AppHttpStatus;
import com.emily.framework.common.exception.BusinessException;
import com.emily.framework.common.exception.PrintExceptionInfo;
import com.emily.framework.common.utils.log.LoggerUtils;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static org.springframework.cloud.gateway.filter.factory.RetryGatewayFilterFactory.RETRY_ITERATION_KEY;

/**
 * @program: EmilyGateway
 * @description: 自定义异常处理
 * @create: 2021/01/06
 */
public class EmilyErrorWebExceptionHandler extends DefaultErrorWebExceptionHandler {

    private final ErrorAttributes errorAttributes;

    public EmilyErrorWebExceptionHandler(ErrorAttributes errorAttributes, WebProperties.Resources resources, ErrorProperties errorProperties, ApplicationContext applicationContext) {
        super(errorAttributes, resources, errorProperties, applicationContext);
        this.errorAttributes = errorAttributes;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable throwable) {
        return super.handle(exchange, throwable);
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes;
        Throwable error = this.getError(request);
        if (error != null) {
            errorAttributes = new LinkedHashMap<>();
            if (error instanceof BusinessException) {
                errorAttributes.put("status", ((BusinessException) error).getStatus());
                errorAttributes.put("messages", ((BusinessException) error).getErrorMessage());
                return errorAttributes;
            } else if (error instanceof ResponseStatusException) {
                errorAttributes.put("status", ((ResponseStatusException) error).getStatus().value());
                errorAttributes.put("messages", ((ResponseStatusException) error).getStatus().getReasonPhrase());
                return errorAttributes;
            } else if(error instanceof TimeoutException){
                errorAttributes.put("status", HttpStatus.GATEWAY_TIMEOUT.value());
                errorAttributes.put("messages", HttpStatus.GATEWAY_TIMEOUT.getReasonPhrase());
                return errorAttributes;
            }

        }
        errorAttributes = this.errorAttributes.getErrorAttributes(request, options.isIncluded(ErrorAttributeOptions.Include.STACK_TRACE));

        if (!options.isIncluded(ErrorAttributeOptions.Include.EXCEPTION)) {
            errorAttributes.remove("exception");
        }

        if (!options.isIncluded(ErrorAttributeOptions.Include.STACK_TRACE)) {
            errorAttributes.remove("trace");
        }

        if (!options.isIncluded(ErrorAttributeOptions.Include.MESSAGE) && errorAttributes.get("message") != null) {
            errorAttributes.put("message", "");
        }

        if (!options.isIncluded(ErrorAttributeOptions.Include.BINDING_ERRORS)) {
            errorAttributes.remove("errors");
        }
        errorAttributes.remove("timestamp");
        errorAttributes.remove("path");
        errorAttributes.put("message", org.apache.commons.lang3.StringUtils.join(errorAttributes.get("error"),
                ",", errorAttributes.get("message"),
                ",", errorAttributes.get("exception"),
                ",", "重试次数：" + request.exchange().getAttributeOrDefault(RETRY_ITERATION_KEY, 0)));
        System.out.println(errorAttributes.get("exception") instanceof BusinessException);
        errorAttributes.remove("exception");
        errorAttributes.remove("error");
        errorAttributes.remove("requestId");

        return errorAttributes;
    }

    @Override
    public ErrorAttributeOptions getErrorAttributeOptions(ServerRequest request, MediaType mediaType) {
        ErrorAttributeOptions options = ErrorAttributeOptions.of(new ErrorAttributeOptions.Include[]{
                ErrorAttributeOptions.Include.EXCEPTION,
                ErrorAttributeOptions.Include.MESSAGE});
        return options;
    }

    @Override
    public void logError(ServerRequest request, ServerResponse response, Throwable throwable) {
        String message = throwable.getMessage();
        LoggerUtils.error(EmilyErrorWebExceptionHandler.class, org.apache.commons.lang3.StringUtils.join(message, "\n\r", PrintExceptionInfo.printErrorInfo(throwable), "\n\r", PrintExceptionInfo.printErrorInfo(throwable.getSuppressed())));
    }

}
