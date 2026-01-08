package com.my.rpc.registry.impl;


import cn.hutool.core.util.StrUtil;
import com.my.rpc.constant.RpcConstant;
import com.my.rpc.factory.SingletonFactory;
import com.my.rpc.registry.ServiceRegistry;
import com.my.rpc.registry.zk.ZkClient;
import com.my.rpc.util.IpUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class ZkServiceRegistry implements ServiceRegistry {

    private final ZkClient zkClient;

    public ZkServiceRegistry() {
        this(SingletonFactory.getInstance(ZkClient.class));
    }

    public ZkServiceRegistry(ZkClient zkClient) {
        this.zkClient = zkClient;
    }

    @Override
    public void registerService(String rpcServiceName, InetSocketAddress address) {
        log.info("服务注册，rpcServiceName:{}, address:{}", rpcServiceName, address);

        // 例如：/my-rpc/UserService/127.0.0.1:8989
        String path = RpcConstant.ZK_RPC_ROOT_PATH + StrUtil.SLASH + rpcServiceName + StrUtil.SLASH + IpUtils.toIpPort(address);
        zkClient.createPersistentNode(path);

    }
}
