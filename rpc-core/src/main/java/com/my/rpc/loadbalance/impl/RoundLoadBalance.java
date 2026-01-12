package com.my.rpc.loadbalance.impl;

import com.my.rpc.dto.RpcReq;
import com.my.rpc.loadbalance.LoadBalance;

import java.util.List;

public class RoundLoadBalance implements LoadBalance {

    private int last = -1;

    /**
     * @param list 注册上的服务
     * @return 按照某种算法选择一个服务
     */
    @Override
    public String select(List<String> list, RpcReq rpcReq) {
        last++;
        last %= list.size();
        return list.get(last);
    }
}
