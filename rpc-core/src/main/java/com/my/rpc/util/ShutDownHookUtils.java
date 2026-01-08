package com.my.rpc.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ShutDownHookUtils {

    public static void clearAll() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("系统结束运行，清理资源");
            ThreadPoolUtils.shutdownAll();
        }));
    }

}
