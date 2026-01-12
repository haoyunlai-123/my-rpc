package com.my.rpc.spi;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 一个loader加载一个接口的实现类
 * @param <T>
 */
@Slf4j
public class CustomLoader <T> {

    private final Class<T> type;
    // 用来存储实现类
    private final Map<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();

    // 用来存储是实现类的对象
    private final Map<String, Holder<T>> OBJECT_CACHE = new ConcurrentHashMap<>();

    public CustomLoader(Class<T> type) {
        this.type = type;
    }
}
