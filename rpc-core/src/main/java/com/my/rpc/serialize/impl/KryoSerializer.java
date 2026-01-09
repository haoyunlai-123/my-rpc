package com.my.rpc.serialize.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.minlog.Log;
import com.my.rpc.dto.RpcReq;
import com.my.rpc.dto.RpcResp;
import com.my.rpc.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Slf4j
public class KryoSerializer implements Serializer {

    // 用ThreadLocal保护kryo,Kryo类型线程不安全
    // withInitial方法返回值作为thread中map的值
    private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.register(RpcReq.class);
        kryo.register(RpcResp.class);
        return kryo;
    });

    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream oos = new ByteArrayOutputStream();
             Output output = new Output(oos);) {

            Kryo kryo = KRYO_THREAD_LOCAL.get();
            kryo.writeObject(output, obj);
            output.flush();

            return oos.toByteArray();

        } catch (Exception e) {
            log.error("kryo序列化失败", e);
            throw new RuntimeException(e);
        } finally {
            KRYO_THREAD_LOCAL.remove();
        }
    }

    @Override
    public <T> T deserializer(byte[] bytes, Class<T> clazz) {

        try (ByteArrayInputStream is = new ByteArrayInputStream(bytes);
             Input input = new Input(is);) {

            Kryo kryo = KRYO_THREAD_LOCAL.get();
            return kryo.readObject(input, clazz);

        } catch (Exception e) {

            log.error("对象反序列化失败", e);
            throw new RuntimeException(e);

        } finally {
            KRYO_THREAD_LOCAL.remove();
        }
    }
}
