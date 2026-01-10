package com.my.rpc.registry.zk;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import com.my.rpc.constant.RpcConstant;
import com.my.rpc.util.IpUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import javax.swing.text.Style;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用来管理和zk的连接
 */
@Slf4j
public class ZkClient {

    // 重试之间等待的初始时间
    private static final int BASE_SLEEP_TIME = 1000;
    // 最大重试次数
    private static final int MAX_RETRIES = 3;
    private final CuratorFramework client;
    // 键 /my-rpc/rpcServiceName，值存当前节点的所有孩子名[127.0.0.1:8888]
    private static final Map<String, List<String>> SERVICE_ADDRESS_CACHE = new ConcurrentHashMap<>();
    // /my-prc/rpcServiceName/127.0.0.1:8888
    private static final Set<String> SERVICE_ADDRESS_SET = ConcurrentHashMap.newKeySet();

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

        if (SERVICE_ADDRESS_SET.contains(path)) {
            log.info("该节点已存在：{}", path);
            return;
        }

        if (! Objects.isNull(client.checkExists().forPath(path))) {
            SERVICE_ADDRESS_SET.add(path);
            log.info("节点---{}---已存在", path);
            return;
        }

        log.info("创建节点：{}", path);
        // 当父节点不存在时, 会自动创建父节点 更推荐使用
        client.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .forPath(path);

        SERVICE_ADDRESS_SET.add(path);
    }

    @SneakyThrows
    public List<String> getChildrenNode(String path) {
        if (StrUtil.isBlank(path)) {
            throw new IllegalArgumentException("path为空");
        }

        if (SERVICE_ADDRESS_CACHE.containsKey(path)) {
            return SERVICE_ADDRESS_CACHE.get(path);
        }

        List<String> children = client.getChildren().forPath(path);
        SERVICE_ADDRESS_CACHE.put(path, children);

        watchNode(path);

        return children;
    }

    /**
     * 只在server端调用，不需要清空set和cache
     * 因为client端绑定了监听器，会自动更新cache,而server端的服务进程结束就清空了
     * @param address 发送给zk要删除的服务节点
     */
    @SneakyThrows
    public void clearAll(InetSocketAddress address) {
        if (Objects.isNull(address)) {
            throw new IllegalArgumentException("address 为空");
        }
        for (String path : SERVICE_ADDRESS_SET) {
            // /my-rpc/rpcServiceName/127.0.0.1:8888
            if (path.endsWith(IpUtils.toIpPort(address))) {
                log.debug("zk删除节点: {}", address);
                // 删除节点及其所有子节点
                try {
                    client.delete().deletingChildrenIfNeeded().forPath(path);
                    // 若子节点为空，删除父节点
                    /*String faPath = path.substring(0, path.lastIndexOf("/"));
                    if (client.getChildren().forPath(faPath).isEmpty()) {
                        client.delete().deletingChildrenIfNeeded().forPath(path);
                    }*/
                } catch (Exception e) {
                    log.error("zk删除节点失败：{}", path, e);
                }
            }
        }
    }

    @SneakyThrows
    // 监听zk的节点数据
    private void watchNode(String path) {
        PathChildrenCache pathChildrenCache = new PathChildrenCache(client, path, true);

        // 给某个节点注册子节点监听器
        PathChildrenCacheListener pathChildrenCacheListener = (client, event) -> {
            /*if (pathChildrenCacheEvent.getType() == PathChildrenCacheEvent.Type.CHILD_REMOVED) {
                System.out.println("子节点删除");
            } else if (pathChildrenCacheEvent.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED) {
                System.out.println("子节点新增");
            } else if (pathChildrenCacheEvent.getType() == PathChildrenCacheEvent.Type.CHILD_UPDATED) {
                System.out.println("子节点修改");
            }*/
            List<String> childs = client.getChildren().forPath(path);
            SERVICE_ADDRESS_CACHE.put(path, childs);
        };

        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        // 启动pathChildrenCache
        pathChildrenCache.start();
    }
}
