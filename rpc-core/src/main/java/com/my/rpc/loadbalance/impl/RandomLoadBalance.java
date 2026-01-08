package com.my.rpc.loadbalance.impl;

import cn.hutool.core.util.RandomUtil;
import com.my.rpc.loadbalance.LoadBalance;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class RandomLoadBalance implements LoadBalance {

    /**
     * @param list 注册上的服务
     * @return 按照某种算法选择一个服务
     */
    @Override
    public String select(List<String> list) {
        return RandomUtil.randomEle(list);
    }
}
