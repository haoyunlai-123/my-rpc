package com.my.rpc.loadbalance.impl;

import cn.hutool.core.util.RandomUtil;
import com.my.rpc.dto.RpcReq;
import com.my.rpc.loadbalance.LoadBalance;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class RandomLoadBalance implements LoadBalance {


    @Override
    public String select(List<String> list, RpcReq rpcReq) {
        return RandomUtil.randomEle(list);
    }
}
