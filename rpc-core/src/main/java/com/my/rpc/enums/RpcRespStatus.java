package com.my.rpc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum RpcRespStatus {
    SUCCESS(0, "success"),
    FAIL(9999, "fail");

    private final int code;
    private final String message;

    public static boolean isSuccessful(int code) {
        return code == SUCCESS.getCode();
    }

    public static boolean isFailed(int code) {
        return ! isSuccessful(code);
    }

}
