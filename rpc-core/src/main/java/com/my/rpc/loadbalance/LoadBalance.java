package com.my.rpc.loadbalance;

import com.my.rpc.dto.RpcReq;

import java.util.List;

public interface LoadBalance {

    public String select(List<String> list, RpcReq rpcReq);

}
