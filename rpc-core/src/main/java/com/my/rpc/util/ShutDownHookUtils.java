package com.my.rpc.util;

import com.my.rpc.factory.SingletonFactory;
import com.my.rpc.registry.ServiceRegistry;
import com.my.rpc.registry.impl.ZkServiceRegistry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ShutDownHookUtils {

    /**
     * jvm关闭时调用
     */
    public static void clearAll() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("系统结束运行，清理资源");
            ServiceRegistry serviceRegistry = SingletonFactory.getInstance(ZkServiceRegistry.class);
            serviceRegistry.clearAll();
            ThreadPoolUtils.shutdownAll();
        }));
    }

}
