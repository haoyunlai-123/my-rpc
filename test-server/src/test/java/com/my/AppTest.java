package com.my;

import lombok.SneakyThrows;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

/**
 * Unit test for simple App.
 */
public class AppTest {
    @SneakyThrows
    public static void main(String[] args) {
        // 重试策略
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(100, 3);

        CuratorFramework zkClient = CuratorFrameworkFactory.builder()
                // 要连接的服务器列表
                .connectString("127.0.0.1:2181")
                .retryPolicy(retryPolicy)
                .build();

        zkClient.start();

        // 创建持久化节点 (默认就是持久化的), 父节点不存在时报错
        /*zkClient.create().forPath("/node1");  // 创建node1父节点
        zkClient.create().forPath("/node1/00001");
        zkClient.create().withMode(CreateMode.PERSISTENT).forPath("/node1/00002");*/

        // 当父节点不存在时, 会自动创建父节点 更推荐使用
        /*zkClient.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .forPath("/node1/00003", "测试数据".getBytes());*/

//        zkClient.delete().deletingChildrenIfNeeded().forPath("/node1");

        // 需要创建zkclient

        /*String path = "/node1";

    // 创建 NodeCache 实例
        NodeCache nodeCache = new NodeCache(zkClient, path);

    // 注册监听器
        NodeCacheListener listener = () -> {
            if (nodeCache.getCurrentData() != null) {
                String data = new String(nodeCache.getCurrentData().getData());
                System.out.println("节点数据变化: " + data);
            } else {
                System.out.println("节点被删除");
            }
        };
        nodeCache.getListenable().addListener(listener);

    // 启动 NodeCache
        nodeCache.start();

    // 模拟程序运行一段时间
//        Thread.sleep(60000);
        System.in.read();
    }*/


        // 需要创建zkclient

        String path = "/node1";

        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, path, true);

        // 给某个节点注册子节点监听器
        PathChildrenCacheListener pathChildrenCacheListener = (curatorFramework, pathChildrenCacheEvent) -> {
            if (pathChildrenCacheEvent.getType() == PathChildrenCacheEvent.Type.CHILD_REMOVED) {
                System.out.println("子节点删除");
            } else if (pathChildrenCacheEvent.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED) {
                System.out.println("子节点新增");
            } else if (pathChildrenCacheEvent.getType() == PathChildrenCacheEvent.Type.CHILD_UPDATED) {
                System.out.println("子节点修改");
            }
        };

        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        // 启动pathChildrenCache
        pathChildrenCache.start();

        // 模拟程序运行一段时间
        Thread.sleep(60000);
    }
}
