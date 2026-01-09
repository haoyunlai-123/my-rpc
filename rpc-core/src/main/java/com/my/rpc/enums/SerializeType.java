package com.my.rpc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;

@Getter
@ToString
@AllArgsConstructor
public enum SerializeType {
    KRYO((byte) 1, "kryo");

    private final byte code;
    private final String desc;

    public static SerializeType from(byte code) {
        return Arrays.stream(values())
                .filter(type -> type.getCode() == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("找不到对应的code" + code));
    }
}
