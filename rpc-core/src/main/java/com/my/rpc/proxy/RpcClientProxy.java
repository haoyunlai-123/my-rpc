package com.my.rpc.proxy;

import cn.hutool.core.util.IdUtil;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.my.rpc.annotation.Breaker;
import com.my.rpc.annotation.Retry;
import com.my.rpc.breaker.CircuitBreaker;
import com.my.rpc.breaker.CircuitBreakerManager;
import com.my.rpc.config.RpcServiceConfig;
import com.my.rpc.dto.RpcReq;
import com.my.rpc.dto.RpcResp;
import com.my.rpc.enums.RpcRespStatus;
import com.my.rpc.exception.RpcException;
import com.my.rpc.transmission.RpcClient;
import com.my.rpc.transmission.socket.client.SocketClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RpcClientProxy implements InvocationHandler {

    private final RpcClient rpcClient;
    private final RpcServiceConfig rpcServiceConfig;

    public RpcClientProxy(RpcClient rpcClient) {
        this(rpcClient, new RpcServiceConfig());
    }

    public RpcClientProxy(RpcClient rpcClient, RpcServiceConfig rpcServiceConfig) {
        this.rpcClient = rpcClient;
        this.rpcServiceConfig = rpcServiceConfig;
    }

//    public <T> T getProxy() {
//        Class<?>[] interfaces = rpcClient.getClass().getInterfaces();
//        if (interfaces.length == 0) {
//            throw new IllegalArgumentException("Target class " + rpcClient.getClass().getName() +
//                    " does not implement any interfaces");
//        }
//
//        Object proxy = Proxy.newProxyInstance(
//                rpcClient.getClass().getClassLoader(),
//                interfaces,
//                this
//        );
//
//        // 通过泛型类型推断来确保类型安全
//        return (T) proxy;
//    }

    // 抑制编译器警告
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class[]{clazz},
                this
        );
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        RpcReq req = RpcReq.builder()
                .reqId(IdUtil.fastSimpleUUID())
                // 注意，这里拿到的是接口的全类名
                .interfaceName(method.getDeclaringClass().getCanonicalName())
                .methodName(method.getName())
                .params(args)
                .paramTypes(method.getParameterTypes())
                .version(rpcServiceConfig.getVersion())
                .group(rpcServiceConfig.getGroup())
                .build();

        Breaker breaker = method.getAnnotation(Breaker.class);
        if (Objects.isNull(breaker)) {
            return sendReqWithRetry(req, method);
        }

        // 此处逻辑有点问题，因为熔断同样是按照接口方法来的，应当存入
        CircuitBreaker circuitBreaker = CircuitBreakerManager.get(req.rpcServiceName(), breaker);
        // CircuitBreaker circuitBreaker = CircuitBreakerManager.get(req.rpcServiceName() + req.getMethodName(), breaker);

        if (! circuitBreaker.canReq()) {
            throw new RpcException("已被熔断处理");
        }

        try {
            Object o = sendReqWithRetry(req, method);
            circuitBreaker.success();
            return o;
        } catch (Exception e) {
            circuitBreaker.fail();
            throw e;
        }

    }

    @SneakyThrows
    private Object sendReqWithRetry(RpcReq req, Method method) {
        Retry retry = method.getAnnotation(Retry.class);
        if (Objects.isNull(retry)) {
            return sendReq(req);
        }

        Retryer<Object> retryer = RetryerBuilder.<Object>newBuilder()
                // 发生何种异常时重试
                .retryIfExceptionOfType(retry.value())
                // 重试次数
                .withStopStrategy(StopStrategies.stopAfterAttempt(retry.maxAttempts()))
                // 重试间隔时间
                .withWaitStrategy(WaitStrategies.fixedWait(retry.delay(), TimeUnit.MILLISECONDS))
                .build();

        return retryer.call(() -> sendReq(req));
    }

    @SneakyThrows
    private Object sendReq(RpcReq req) {
        Future<RpcResp<?>> future = rpcClient.sendReq(req);
        RpcResp<?> rpcResp = future.get();
        check(req, rpcResp);
        return rpcResp.getData();
    }


    private void check(RpcReq req, RpcResp<?> rpcResp) {
        if (Objects.isNull(rpcResp)) {
            throw new RpcException("rpcResp为空");
        }

        // 有非空校验
        if (! Objects.equals(req.getReqId(), rpcResp.getReqId())) {
            throw new RpcException("请求和响应的id不一致");
        }

        if (RpcRespStatus.isFailed(rpcResp.getCode())) {
            throw new RpcException("响应值为失败：" + rpcResp.getMsg());
        }
    }
}
