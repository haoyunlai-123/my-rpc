package com.my.rpc.transmission;

import com.my.rpc.config.RpcServiceConfig;

public interface RpcServer {

    // 由服务提供方server引入
    void start();

    // 发布服务
    void publishService(RpcServiceConfig config);
}
