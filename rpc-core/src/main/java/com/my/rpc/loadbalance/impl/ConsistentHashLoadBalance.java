package com.my.rpc.loadbalance.impl;

import com.google.common.hash.Hashing;
import com.my.rpc.dto.RpcReq;
import com.my.rpc.loadbalance.LoadBalance;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class ConsistentHashLoadBalance implements LoadBalance {

    @Override
    public String select(List<String> list, RpcReq rpcReq) {

        String key = rpcReq.rpcServiceName();
        // 计算key在哈希环上的映射
        long hashCode = Hashing.murmur3_128().hashString(key, StandardCharsets.UTF_8).asLong();
        // 计算key在哈希环上映射的下一个节点的位置
        int index = Hashing.consistentHash(hashCode, list.size());

        return list.get(index);
    }
}
