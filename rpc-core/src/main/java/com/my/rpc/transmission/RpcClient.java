package com.my.rpc.transmission;

import com.my.rpc.dto.RpcReq;
import com.my.rpc.dto.RpcResp;

import java.util.concurrent.Future;

public interface RpcClient {

    // 向服务提供方发送请求，由client端引入
    // 返回Future的好处是把选择同步还是异步的权力交给了上层
    Future<RpcResp<?>> sendReq(RpcReq req);

}
