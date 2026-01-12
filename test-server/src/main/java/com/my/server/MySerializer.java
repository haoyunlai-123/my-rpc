package com.my.server;

import com.my.rpc.serialize.Serializer;

public class MySerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) {
        return new byte[0];
    }

    @Override
    public <T> T deserializer(byte[] bytes, Class<T> clazz) {
        return null;
    }
}
