package com.my.rpc.serialize.impl;

import com.my.rpc.serialize.Serializer;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProtostuffSerializer implements Serializer {

    private static final LinkedBuffer BUFFER = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);

    @Override
    public byte[] serialize(Object obj) {
        Class<?> aClass = obj.getClass();

        Schema schema = RuntimeSchema.getSchema(aClass);

        log.info("===========使用protostuff做序列化============");

        try {
            return ProtobufIOUtil.toByteArray(obj, schema, BUFFER);
        } finally {
            BUFFER.clear();
        }
    }

    @Override
    public <T> T deserializer(byte[] bytes, Class<T> clazz) {
        Schema<T> schema = RuntimeSchema.getSchema(clazz);
        T t = schema.newMessage();
        ProtobufIOUtil.mergeFrom(bytes, t, schema);

        log.info("===========使用protostuff做反序列化============");

        return t;
    }
}
