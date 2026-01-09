package com.my.rpc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;

@Getter
@ToString
@AllArgsConstructor
public enum MsgType {

    HEARTBEAT_REQ((byte) 1, "心跳请求"),
    HEARTBEAT_RESP((byte) 2, "心跳响应"),
    RPC_REQ((byte) 3, "rpc请求"),
    RPC_RESP((byte) 4, "rpc响应");

    private final byte code;
    private final String desc;

    public boolean isHeartBeat() {
        return this == HEARTBEAT_REQ || this == HEARTBEAT_RESP;
    }

    public boolean isReq() {
        return this == RPC_REQ || this == HEARTBEAT_REQ;
    }

    public static MsgType from(byte code) {
        return Arrays.stream(values())
                .filter(msgType -> msgType.getCode() == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("找不到对应的code" + code));
    }
}
