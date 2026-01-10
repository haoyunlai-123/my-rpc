package com.my.client.utils;

import com.my.rpc.factory.SingletonFactory;
import com.my.rpc.proxy.RpcClientProxy;
import com.my.rpc.transmission.RpcClient;
import com.my.rpc.transmission.netty.client.NettyClient;

public class ProxyUtils {
    private static final RpcClient RPC_CLIENT = SingletonFactory.getInstance(NettyClient.class);
    private static final RpcClientProxy  RPC_CLIENT_PROXY = new RpcClientProxy(RPC_CLIENT);

    public static <T> T getProxy(Class<T> clazz) {
        return RPC_CLIENT_PROXY.getProxy(clazz);
    }
}
