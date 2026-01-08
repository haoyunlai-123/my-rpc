package com.my.rpc.provider.impl;

import cn.hutool.core.util.StrUtil;
import com.my.rpc.config.RpcServiceConfig;
import com.my.rpc.constant.RpcConstant;
import com.my.rpc.factory.SingletonFactory;
import com.my.rpc.provider.ServiceProvider;
import com.my.rpc.registry.ServiceRegistry;
import com.my.rpc.registry.impl.ZkServiceRegistry;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.yetus.audience.InterfaceAudience;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.sql.SQLType;
import java.sql.Struct;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ZkServiceProvider implements ServiceProvider {

    private final Map<String, Object> SERVICE_CACHE = new HashMap<>();
    private final ServiceRegistry serviceRegistry;

    public ZkServiceProvider() {
        this(SingletonFactory.getInstance(ZkServiceRegistry.class));
    }

    public ZkServiceProvider(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * 发布服务
     *
     * @param config
     */
    @Override
    public void publishService(RpcServiceConfig config) {
        config.rpcServiceNames().forEach(serviceName -> {
            publishService(serviceName, config.getService());
        });
    }

    /**
     * 获取服务
     * 此方法只用于在服务提供方获取
     * @param rpcServiceName
     * @return
     */
    @Override
    public Object getService(String rpcServiceName) {
        if (StrUtil.isBlank(rpcServiceName)) {
            throw new IllegalArgumentException("参数为空");
        }

        if (! SERVICE_CACHE.containsKey(rpcServiceName)) {
            throw new IllegalArgumentException("rpcServiceName未注册：" + rpcServiceName);
        }

        return SERVICE_CACHE.get(rpcServiceName);
    }

    @SneakyThrows
    private void publishService(String serviceName, Object service) {
        // 存至zookeeper
        // 1.得到当前服务的ip和端口
        String host = InetAddress.getLocalHost().getHostAddress();
        int port =  RpcConstant.SERVER_PORT;
        InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);
        serviceRegistry.registerService(serviceName, inetSocketAddress);

        // 存至map
        SERVICE_CACHE.put(serviceName, service);
    }
}
