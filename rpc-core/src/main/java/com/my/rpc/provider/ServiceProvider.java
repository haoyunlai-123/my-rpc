package com.my.rpc.provider;

import com.my.rpc.config.RpcServiceConfig;

/**
 * 发布服务，注册服务
 */
public interface ServiceProvider {

    /**
     * 发布服务
     * @param config
     */
    void publishService(RpcServiceConfig config);

    /**
     * 获取服务
     * @param rpcServiceName
     * @return
     */
    Object getService(String rpcServiceName);

}
