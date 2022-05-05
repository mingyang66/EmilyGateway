package com.emily.infrastructure.gateway.common.entity;

import java.io.Serializable;

/**
 * @author Emily
 * @Description: 控制器返回结果
 * @ProjectName: spring-parent
 * @Date: 2019/7/1 15:33
 * @Version: 1.0
 */
public class BaseResponse<T> implements Serializable {
    private int status;
    private String message;
    private T data;
    private long time;

    public BaseResponse() {
        super();
    }

    public BaseResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public BaseResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public BaseResponse(int status, String message, T data, long time) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.time = time;
    }


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public static BaseResponse build(int status, String message) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setStatus(status);
        baseResponse.setMessage(message);
        return baseResponse;
    }
}
