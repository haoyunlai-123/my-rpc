package com.my.rpc.handler;

import com.my.rpc.dto.RpcReq;
import com.my.rpc.provider.ServiceProvider;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

@Slf4j
public class RpcReqHandler {
    private final ServiceProvider serviceProvider;

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
        return method.invoke(service, req.getParams());
    }
}
