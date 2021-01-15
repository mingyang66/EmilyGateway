package com.emily.cloud.gateway.utils;

import io.netty.buffer.ByteBufAllocator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;

/**
 * @program: EmilyGateway
 * @description: DataBuffer工具方法
 * @author: 洋
 * @create: 2021/01/15
 */
public class DataBufferUtils {
    /**
     * string转DataBuffer
     *
     * @param value 字符串
     */
    public static Flux<DataBuffer> stringToDataBuffer(String value) {
        if (StringUtils.isEmpty(value)) {
            return Flux.empty();
        }
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        NettyDataBufferFactory nettyDataBufferFactory = new NettyDataBufferFactory(ByteBufAllocator.DEFAULT);
        DataBuffer buffer = nettyDataBufferFactory.allocateBuffer(bytes.length);
        buffer.write(bytes);
        return Flux.just(buffer);
    }

    /**
     * DataBuffer对象转string
     *
     * @param dataBuffer 缓存对象
     */
    public static String dataBufferToString(DataBuffer dataBuffer) {
        if (dataBuffer == null) {
            return null;
        }
        //新建存放响应体的字节数组
        byte[] content = new byte[dataBuffer.readableByteCount()];
        //将响应数据读取到字节数组
        dataBuffer.read(content);
        //释放内存
        org.springframework.core.io.buffer.DataBufferUtils.release(dataBuffer);
        // 获取请求body
        return new String(content, StandardCharsets.UTF_8);
    }
}
