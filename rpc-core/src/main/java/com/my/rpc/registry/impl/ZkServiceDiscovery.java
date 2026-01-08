package com.my.rpc.registry.impl;

import cn.hutool.core.util.StrUtil;
import com.my.rpc.constant.RpcConstant;
import com.my.rpc.dto.RpcReq;
import com.my.rpc.factory.SingletonFactory;
import com.my.rpc.loadbalance.LoadBalance;
import com.my.rpc.loadbalance.impl.RandomLoadBalance;
import com.my.rpc.registry.ServiceDiscovery;
import com.my.rpc.registry.zk.ZkClient;
import com.my.rpc.util.IpUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class ZkServiceDiscovery implements ServiceDiscovery {

    private final ZkClient zkClient;
    private final LoadBalance loadBalance;

    public ZkServiceDiscovery() {
        this(
                SingletonFactory.getInstance(ZkClient.class),
                SingletonFactory.getInstance((RandomLoadBalance.class))
        );
    }

    public ZkServiceDiscovery(ZkClient zkClient, LoadBalance loadBalance) {
        this.zkClient = zkClient;
        this.loadBalance = loadBalance;
    }

    @Override
    public InetSocketAddress lookupService(RpcReq rpcReq) {

        String path = RpcConstant.ZK_RPC_ROOT_PATH +
                StrUtil.SLASH +
                rpcReq.rpcServiceName();

        List<String> childrenNode = zkClient.getChildrenNode(path);

        String ipAndPort = loadBalance.select(childrenNode);

        return IpUtils.toInetSocketAddress(ipAndPort);

    }
}
