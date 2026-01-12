package com.my.api;

import com.my.rpc.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

@Slf4j
public class MySerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream ops = new ByteArrayOutputStream();
                ObjectOutputStream os = new ObjectOutputStream(ops)) {
            log.info("============使用自定义序列化器序列化==============");
            os.writeObject(obj);
            os.flush();
            return ops.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T deserializer(byte[] bytes, Class<T> clazz) {

        try (ByteArrayInputStream ips = new ByteArrayInputStream(bytes);
             ObjectInputStream is = new ObjectInputStream(ips)) {

            log.info("============使用自定义序列化器反序列化==============");

            return clazz.cast(is.readObject());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
