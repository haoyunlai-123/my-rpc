package com.my.rpc.breaker;


import com.my.rpc.annotation.Breaker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CircuitBreakerManager {

    // 建立接口方法到熔断器对象的映射
    private static final Map<String, CircuitBreaker> BREAKER_MAP = new ConcurrentHashMap<>();

    public static CircuitBreaker get(String key, Breaker breaker) {
        return BREAKER_MAP.computeIfAbsent(key, __ -> new CircuitBreaker(
           breaker.failThreshold(),
           breaker.successRateInHaltOpen(),
           breaker.windowTime()
        ));
    }

}
