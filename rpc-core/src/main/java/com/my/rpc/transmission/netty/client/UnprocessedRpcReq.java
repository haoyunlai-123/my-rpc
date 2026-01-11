package com.my.rpc.transmission.netty.client;

import com.my.rpc.dto.RpcResp;
import com.my.rpc.exception.RpcException;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class UnprocessedRpcReq {

    // 用来存储异步任务
    private static final Map<String, CompletableFuture<RpcResp<?>>> RESP_CF_MAP=
            new ConcurrentHashMap<>();

    // 此处存的占位符
    public static void put(String reqId, CompletableFuture<RpcResp<?>> future) {
        RESP_CF_MAP.put(reqId, future);
    }

    public static void complete(RpcResp<?> resp) {
        CompletableFuture<RpcResp<?>> future = RESP_CF_MAP.remove(resp.getReqId());

        if (Objects.isNull(future)) {
            throw new RpcException("不存在相应的CompletableFuture");
        }

        // 此处存真正的值
        future.complete(resp);
    }
}
