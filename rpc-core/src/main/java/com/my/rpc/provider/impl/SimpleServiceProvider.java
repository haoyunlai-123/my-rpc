package com.my.rpc.provider.impl;

import ch.qos.logback.classic.pattern.RootCauseFirstThrowableProxyConverter;
import com.my.rpc.config.RpcServiceConfig;
import com.my.rpc.provider.ServiceProvider;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SimpleServiceProvider implements ServiceProvider {

    private final Map<String, Object> SERVICE_CACHE = new HashMap<>();

    /**
     * 发布服务
     *
     * @param config
     */
    @Override
    public void publishService(RpcServiceConfig config) {
        for (String name : config.rpcServiceNames()) {
            SERVICE_CACHE.put(name, config.getService());
            log.debug("发布服务：{}", name);
        }
    }

    /**
     * 获取服务
     *
     * @param rpcServiceName
     * @return
     */
    @Override
    public Object getService(String rpcServiceName) {
        if (! SERVICE_CACHE.containsKey(rpcServiceName)) {
            throw new IllegalArgumentException("找不到对应的微服务");
        }

        return SERVICE_CACHE.get(rpcServiceName);
    }
}
