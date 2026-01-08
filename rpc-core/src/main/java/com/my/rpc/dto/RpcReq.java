package com.my.rpc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RpcReq implements Serializable {
    private static final Long serialVersionUID = 1L;

    // 用来定位接口的某个实现类
    // 请求id
    private String reqId;
    // 找相应接口的实现类
    private String interfaceName;
    // 找要调用的方法名
    private String methodName;
    // 参数
    private Object[] params;
    // 参数类型
    private Class<?>[] paramTypes;
    // 版本 一个接口可能有多个版本的实现，声明要调用哪个版本
    private String version;
    // 分组 一个接口可能有多个不同类型的实现，声明要调用哪个类型
    private String group;

    /**
     * 返回服务名
     * @return
     */
    public String rpcServiceName() {
        return this.getInterfaceName() + (version == null ? "" : version) + (group == null ? "" : group);
    }
}
