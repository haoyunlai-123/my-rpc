package com.my.rpc.serialize.impl;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.my.rpc.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

@Slf4j
public class HessianSerializer implements Serializer {

    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream oos = new ByteArrayOutputStream()) {

            HessianOutput output = new HessianOutput(oos);
            output.writeObject(obj);

            log.info("===========使用hessian做序列化============");

            return oos.toByteArray();

        } catch (Exception e) {
            log.error("hessian序列化失败", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T deserializer(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream ins = new ByteArrayInputStream(bytes)) {

            HessianInput input = new HessianInput(ins);
            Object o = input.readObject();

            log.info("===========使用hessian做反序列化============");

            return clazz.cast(o);

        } catch (Exception e) {
            log.error("hessian反序列化失败", e);
            throw new RuntimeException(e);
        }
    }
}
