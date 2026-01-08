package com.my.rpc.util;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public final class ThreadPoolUtils {

    // 线程池名字为key,线程池为value
    private static final Map<String, ExecutorService> THREAD_POOL_CACHE = new ConcurrentHashMap<>();

    // CPU数量
    private static final int CPU_NUMS = Runtime.getRuntime().availableProcessors();

    // cpu密集型线程数和io密集型线程数
    private static final int CPU_INTENSIVE_NUMS = CPU_NUMS + 1;
    private static final int IO_INTENSIVE_NUMS = CPU_NUMS * 2;
    private static final int DEFAULT_KEEP_ALIVE_TIME = 60;
    private static final int DEFAULT_QUEUE_SIZE = 128;

    public static ExecutorService createIoIntensiveThreadPool(
            String poolName
    ) {

        return createThreadPool(IO_INTENSIVE_NUMS, poolName);
    }

    public static ExecutorService createCpuIntensiveThreadPool(
            String poolName
    ) {

        return createThreadPool(CPU_INTENSIVE_NUMS, poolName);
    }

    public static ExecutorService createThreadPool(
            int corePoolSize,
            String poolName
    ) {
        return createThreadPool(corePoolSize, corePoolSize, poolName);
    }

    public static ExecutorService createThreadPool(
            int corePoolSize,
            int maxPoolSize,
            String poolName
    ) {
        return createThreadPool(corePoolSize, maxPoolSize, DEFAULT_KEEP_ALIVE_TIME, DEFAULT_QUEUE_SIZE, poolName);
    }

    public static ExecutorService createThreadPool(
            int corePoolSize,
            int maxPoolSize,
            long keepAliveTime,
            int queueSize,
            String poolName
    ) {
        return createThreadPool(corePoolSize, maxPoolSize, keepAliveTime, queueSize, poolName, false);
    }

    public static ExecutorService createThreadPool(
            int corePoolSize,
            int maxPoolSize,
            long keepAliveTime,
            int queueSize,
            String poolName,
            boolean isDaemon
    ) {
        if (THREAD_POOL_CACHE.containsKey(poolName)) {
            return THREAD_POOL_CACHE.get(poolName);
        }

        ExecutorService executorService = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueSize),
                createThreadFactory(poolName, isDaemon)
        );

        THREAD_POOL_CACHE.put(poolName, executorService);
        return executorService;
    }

    public static ThreadFactory createThreadFactory(String poolName) {
        return createThreadFactory(poolName, false);
    }

    public static ThreadFactory createThreadFactory(String poolName, boolean isDaemon) {
        ThreadFactoryBuilder threadFactoryBuilder = ThreadFactoryBuilder.create()
                .setDaemon(isDaemon);
        if (StrUtil.isBlank(poolName)) {
            return threadFactoryBuilder.build();
        }
        return threadFactoryBuilder.setNamePrefix(poolName).build();
    }


    public static void shutdownAll() {
        // 并行流，并行处理流中的数据
        THREAD_POOL_CACHE.entrySet().parallelStream().forEach(entry -> {
            String name = entry.getKey();
            ExecutorService executorService = entry.getValue();
            log.info("线程池--{}--开始停止.......", name);
            executorService.shutdown();
            try {
                // 判断线程池10秒内是否停止
                if (executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    log.info("线程池--{}--已经停止", name);
                } else {
                    log.info("线程池--{}--10秒内未停止", name);
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.error("线程池--{}--停止异常", executorService);
                executorService.shutdownNow();
            }
        });
    }

}
