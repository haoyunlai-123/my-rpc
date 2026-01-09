package com.my.rpc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@AllArgsConstructor
@Getter
public enum VersionType {
    VERSION1((byte) 1, "版本1");

    private final byte code;
    private final String desc;

}
