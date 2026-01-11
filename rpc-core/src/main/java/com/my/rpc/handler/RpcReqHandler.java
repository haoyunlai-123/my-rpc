package com.my.rpc.handler;

import com.my.rpc.annotation.Limit;
import com.my.rpc.dto.RpcReq;
import com.my.rpc.exception.RpcException;
import com.my.rpc.provider.ServiceProvider;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.shaded.com.google.common.util.concurrent.RateLimiter;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RpcReqHandler {
    private final ServiceProvider serviceProvider;

    // 对Method和相应的RateLimiter做映射，每个方法对应一个限流器对象
    private static final Map<String, RateLimiter> RATE_LIMITER_MAP = new ConcurrentHashMap<>();

//    private static final Map<String, RateLimiter> RATE_LIMITER_MAP = new ConcurrentHashMap<>();

    public RpcReqHandler(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    // lombok的注解，能够自动捕获异常并抛出
    @SneakyThrows
    public Object invoke(RpcReq req) {
        String rpcServiceName = req.rpcServiceName();
        log.debug("传入的请求参数为：{}", rpcServiceName);
        Object service = serviceProvider.getService(rpcServiceName);

        // 即获取到传来的参数的接口 + version + group 所对应的实现类对象（能够唯一定位实现类对象）
        log.debug("获取到对应的服务：{}", service.getClass().getCanonicalName());
        Method method = service.getClass().getMethod(req.getMethodName(), req.getParamTypes());

        Limit limit = method.getAnnotation(Limit.class);
        if (Objects.isNull(limit)) {
            method.invoke(service, req.getParams());
        }

        // 此处可以优化，应该建立类中具体方法到限流器的映射
        // 也可以提前建立好map，防止第一次访问大多数请求被限流
        RateLimiter rateLimiter = RATE_LIMITER_MAP.computeIfAbsent(rpcServiceName, key ->
                RateLimiter.create(limit.permitsPerSecond())
        );
        /*RateLimiter rateLimiter = RATE_LIMITER_MAP.computeIfAbsent(rpcServiceName + req.getMethodName(), key ->
                RateLimiter.create(limit.permitsPerSecond())
        );*/

        if (! rateLimiter.tryAcquire(limit.timeout(), TimeUnit.MILLISECONDS)) {
            throw new RpcException("系统繁忙, 请稍后重试");
        }

        return method.invoke(service, req.getParams());
    }
}
