package com.my.rpc.util;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.setting.dialect.Props;
import com.my.rpc.config.RpcConfig;

import java.util.Objects;

public class ConfigUtils {

    private static final String CONFIG_FILE_NAME = "rpc-config.properties";
    private static RpcConfig RPC_CONFIG;

    private ConfigUtils() {
    }

    private static void loadConfig() {
        // 资源不存在
        if (ResourceUtil.getResource(CONFIG_FILE_NAME) == null) {
            RPC_CONFIG = new RpcConfig();
            return;
        }

        // 资源为空
        Props props = new Props(CONFIG_FILE_NAME);
        if (props.isEmpty()) {
            RPC_CONFIG = new RpcConfig();
            return;
        }

        RPC_CONFIG = props.toBean(RpcConfig.class);
    }

    public static RpcConfig getRpcConfig() {
        if (Objects.isNull(RPC_CONFIG)) {
            loadConfig();
        }

        return RPC_CONFIG;
    }
}
