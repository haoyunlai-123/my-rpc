package com.my.rpc.registry;

import java.net.InetSocketAddress;

/**
 * 用来发布服务，清理服务
 */
public interface ServiceRegistry {

    void registerService(String rpcServiceName, InetSocketAddress address);

    void clearAll();

}
