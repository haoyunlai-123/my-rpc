package com.my.rpc.spi;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import com.my.rpc.serialize.Serializer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 一个loader加载一个接口的实现类
 * @param <T>
 */
@Slf4j
public class CustomLoader <T> {

    private static final String BASE_PATH = "META-INF/my-rpc/";

    private final Class<T> type;
    // 用来存储实现类
    private final Map<String, Class<T>> CLASS_CACHE = new ConcurrentHashMap<>();

    // 用来存储是实现类的对象
    private final Map<String, Holder<T>> OBJECT_CACHE = new ConcurrentHashMap<>();

    // 静态Map用来存储接口和相应的Loader的映射
    // 这里存储任意类型，所以泛型为<?>
    private static final Map<Class<?>, CustomLoader<?>> LOADER_CACHE = new ConcurrentHashMap<>();

    public CustomLoader(Class<T> type) {
        this.type = type;
    }

    public static <V> CustomLoader<V> getLoader(Class<V> clazz) {
        if (Objects.isNull(clazz)) {
            throw new IllegalArgumentException("clazz为null");
        }

        if (! clazz.isInterface()) {
            throw new IllegalArgumentException("clazz必须为接口");
        }

        // 这里不用双重检查锁， 因为为ConcurrentHashMap
        CustomLoader<?> loader = LOADER_CACHE.get(clazz);
        if (loader == null) {
            loader = new CustomLoader<V>(clazz);
            LOADER_CACHE.put(clazz, loader);
        }

        return (CustomLoader<V>) loader;
    }

    public T get(String name) {
        if (StrUtil.isBlank(name)) {
            throw new IllegalArgumentException("name为空");
        }
        Holder<T> holder = OBJECT_CACHE.computeIfAbsent(name, __ -> new Holder<>());

        T t = holder.get();
        // 双重检查锁
        if (t == null) {
            synchronized (holder) {
                t = holder.get();
                if (t == null) {
                    t = createObj(name);
                    holder.set(t);
                }
            }
        }

        return t;
    }

    @SneakyThrows
    private T createObj(String name) {
        if (CollUtil.isEmpty(CLASS_CACHE)) {
            loadDir();
        }

        // 因为用当前的类加载器加载的，在同一条类加载器链上（同一个命名空间），所以加载的类被认为是接口的实现类
        Class<T> clazz = CLASS_CACHE.get(name);
        return clazz.newInstance();
    }

    @SneakyThrows
    private void loadDir() {
        // "META-INF/my-rpc/com.my.rpc.serialize.Serializer"
        String path = BASE_PATH + type.getCanonicalName();
        ClassLoader classLoader = CustomLoader.class.getClassLoader();
        // 加载classpath下的资源
        Enumeration<URL> urls = classLoader.getResources(path);

        if (CollUtil.isEmpty(urls)) {
            throw new RuntimeException("资源不存在：" + path);
        }

        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            loadResource(classLoader, url);
        }
    }

    @SneakyThrows
    private void loadResource(ClassLoader classLoader, URL url) {
        // 此处用BufferedReader因为里面有一行一行读取的方法
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                url.openStream(), StandardCharsets.UTF_8))) {
            String line;

            while ((line = reader.readLine()) != null) {
                Pair<String, Class<T>> pair = handleLine(classLoader, line);

                if (Objects.isNull(pair)) {
                    continue;
                }

                CLASS_CACHE.put(pair.getKey(), pair.getValue());
            }
        }
    }

    @SneakyThrows
    private Pair<String, Class<T>> handleLine(ClassLoader classLoader, String line) {
        line = line.trim();

        if (StrUtil.isBlank(line)) {
            return null;
        }

        String[] split = line.split("=");
        if (split.length != 2) {
            throw new RuntimeException("行数据异常");
        }

        Class<T> clazz = (Class<T>) classLoader.loadClass(split[1]);
        return new Pair<>(split[0], clazz);
    }

    public static void main(String[] args) {
        CustomLoader<Serializer> loader = CustomLoader.getLoader(Serializer.class);
        Serializer serializer = loader.get("kryo");
        System.out.println(serializer);
    }
}
