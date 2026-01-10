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
        // 因为底层的资源用的是操作系统层面的，所以jvm关闭时必须释放资源占用
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("系统结束运行，清理资源");
            ServiceRegistry serviceRegistry = SingletonFactory.getInstance(ZkServiceRegistry.class);
            serviceRegistry.clearAll();
            ThreadPoolUtils.shutdownAll();
        }));
    }

}
