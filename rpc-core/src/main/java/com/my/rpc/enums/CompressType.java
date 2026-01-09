package com.my.rpc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum CompressType {
    GZIP((byte) 1, "gzip");

    private final byte code;
    private final String desc;
}
