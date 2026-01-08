package com.my.rpc.registry;

import com.my.rpc.dto.RpcReq;

import java.net.InetSocketAddress;

public interface ServiceDiscovery {

    InetSocketAddress lookupService(RpcReq rpcReq);

}
