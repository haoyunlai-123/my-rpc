package com.my.rpc.transmission;

import com.my.rpc.dto.RpcReq;
import com.my.rpc.dto.RpcResp;

public interface RpcClient {

    // 向服务提供方发送请求，由client端引入
    RpcResp<?> sendReq(RpcReq req);

}
