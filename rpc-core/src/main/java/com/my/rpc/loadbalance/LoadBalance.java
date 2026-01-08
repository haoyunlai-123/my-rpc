package com.my.rpc.loadbalance;

import java.util.List;

public interface LoadBalance {
    /**
     *
     * @param list 注册上的服务
     * @return 按照某种算法选择一个服务
     */
    public String select(List<String> list);

}
