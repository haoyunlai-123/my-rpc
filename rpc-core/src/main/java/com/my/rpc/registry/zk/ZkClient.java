package com.my.rpc.registry.zk;

import cn.hutool.core.util.StrUtil;
import com.my.rpc.constant.RpcConstant;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.List;
import java.util.Objects;

@Slf4j
public class ZkClient {

    // 重试之间等待的初始时间
    private static final int BASE_SLEEP_TIME = 1000;
    // 最大重试次数
    private static final int MAX_RETRIES = 3;
    private CuratorFramework client;

    public ZkClient() {
        this(RpcConstant.ZK_IP, RpcConstant.ZK_PORT);
    }

    public ZkClient(String hostname, int port) {
        // 重试策略
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);

        client = CuratorFrameworkFactory.builder()
                // 要连接的服务器列表
                .connectString(hostname + StrUtil.COLON + port)
                .retryPolicy(retryPolicy)
                .build();

        log.info("zk开始连接......");
        client.start();
        log.info("zk连接成功.....");
    }

    @SneakyThrows
    public void createPersistentNode(String path) {

        if (StrUtil.isBlank(path)) {
            throw new IllegalArgumentException("path为空");
        }

        if (! Objects.isNull(client.checkExists().forPath(path))) {
            log.info("节点---{}---已存在", path);
            return;
        }

        log.info("创建节点：{}", path);
        // 当父节点不存在时, 会自动创建父节点 更推荐使用
        client.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .forPath(path);
    }

    @SneakyThrows
    public List<String> getChildrenNode(String path) {
        if (StrUtil.isBlank(path)) {
            throw new IllegalArgumentException("path为空");
        }
        return client.getChildren().forPath(path);
    }
}
