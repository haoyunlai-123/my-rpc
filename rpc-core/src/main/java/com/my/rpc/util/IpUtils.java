package com.my.rpc.util;

import cn.hutool.core.util.StrUtil;
import com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Objects;

@Slf4j
public class IpUtils {

    // ip:port
    public static String toIpPort(InetSocketAddress address) {
        if (Objects.isNull(address)) {
            throw new IllegalArgumentException("address参数为空");
        }

        String host = address.getHostString();
        if (Objects.equals(host, "localhost")) {
            host = "127.0.0.1";
        }

        return host + StrUtil.COLON + address.getPort();
    }

    public static InetSocketAddress toInetSocketAddress(String ipAndPort) {
        if (StrUtil.isBlank(ipAndPort)) {
            throw new IllegalArgumentException("ipAndPort参数为空");
        }

        String[] addr = ipAndPort.split(StrUtil.COLON);
        if (addr.length != 2) {
            throw new IllegalArgumentException("ipAndPort格式异常");
        }
        String ip = addr[0];
        String port = addr[1];
        return new InetSocketAddress(ip, Integer.parseInt(port));
    }

}
