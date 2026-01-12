package com.my.rpc.spi;

/**
 * 用来保存实现类对象
 * @param <T>
 */
public class Holder <T> {

    private volatile T value;

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}
