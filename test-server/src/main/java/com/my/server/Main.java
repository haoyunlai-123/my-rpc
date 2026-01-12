package com.my.server;

import com.my.api.UserService;
import com.my.rpc.config.RpcServiceConfig;
import com.my.rpc.dto.RpcReq;
import com.my.rpc.factory.SingletonFactory;
import com.my.rpc.loadbalance.LoadBalance;
import com.my.rpc.loadbalance.impl.ConsistentHashLoadBalance;
import com.my.rpc.proxy.RpcClientProxy;
import com.my.rpc.transmission.RpcServer;
import com.my.rpc.transmission.netty.server.NettyRpcServer;
import com.my.rpc.transmission.socket.server.SocketRpcServer;
import com.my.server.service.UserServiceImpl;

import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        /*RpcServer rpcServer = new RpcServer() {
            @Override
            public void start() {

            }
        };
        rpcServer.start();*/

//        SocketRpcServer socketRpcServer = new SocketRpcServer(8888);

        /*RpcServiceConfig config = new RpcServiceConfig(new UserServiceImpl());
        // 服务方必须将实现类存至键值对中
        // 键值对中存的是： "UserService" : new UserServiceImpl()
        RpcServer server = new NettyRpcServer();
        server.publishService(config);
        server.start();*/

        LoadBalance loadBalance = SingletonFactory.getInstance(ConsistentHashLoadBalance.class);

        List<String> list = Arrays.asList("ip1:port1", "ip2:port2", "ip3:port3");

        RpcReq rpcReq = RpcReq.builder()
                .interfaceName("test")
                .version("version")
                .group("group")
                .build();

        for (int i = 0; i < 10; i++) {
            String select = loadBalance.select(list, rpcReq);
            System.out.println(select);
        }
    }

    public static void main1(String[] args) {
        /*RpcClientProxy proxy = new RpcClientProxy(new UserServiceImpl());
        UserService userService = (UserService) proxy.getProxy();
        userService.getUser(1L);*/

        /*RpcServiceConfig config = new RpcServiceConfig(new UserServiceImpl());
        SocketRpcServer rpcServer = new SocketRpcServer();
        rpcServer.publishService(config);
        rpcServer.start();*/

        /*NettyRpcServer server = new NettyRpcServer();
        server.start();*/

    }

}
