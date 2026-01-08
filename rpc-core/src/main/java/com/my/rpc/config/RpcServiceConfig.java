package com.my.rpc.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpcServiceConfig {

    private String version = "";
    private String group = "";
    // 存放接口的实现类
    private Object service;

    public RpcServiceConfig(Object service) {
        this.service = service;
    }

    /**
     * 返回全部接口的全类名 + version + group
     * @return
     */
    public List<String> rpcServiceNames() {
        return interfaceNames().stream()
                .map(name -> name + this.getVersion() + this.getGroup())
                .collect(Collectors.toList());
    }

    /**
     * 获取实现类service的全部接口的全类名
     * @return
     */
    private List<String> interfaceNames() {
        return Arrays.stream(service.getClass().getInterfaces())
                .map(Class::getCanonicalName)
                .collect(Collectors.toList());
    }
}
