package com.my.server;

import com.my.api.UserService;
import com.my.rpc.config.RpcServiceConfig;
import com.my.rpc.proxy.RpcClientProxy;
import com.my.rpc.transmission.RpcServer;
import com.my.rpc.transmission.socket.server.SocketRpcServer;
import com.my.server.service.UserServiceImpl;

public class Main {

    public static void main1(String[] args) {
        /*RpcServer rpcServer = new RpcServer() {
            @Override
            public void start() {

            }
        };
        rpcServer.start();*/

        SocketRpcServer socketRpcServer = new SocketRpcServer(8888);

        RpcServiceConfig config = new RpcServiceConfig(new UserServiceImpl());

        // 服务方必须将实现类存至键值对中
        // 键值对中存的是： "UserService" : new UserServiceImpl()
        socketRpcServer.publishService(config);
        socketRpcServer.start();
    }

    public static void main(String[] args) {
        /*RpcClientProxy proxy = new RpcClientProxy(new UserServiceImpl());
        UserService userService = (UserService) proxy.getProxy();
        userService.getUser(1L);*/

        RpcServiceConfig config = new RpcServiceConfig(new UserServiceImpl());
        SocketRpcServer rpcServer = new SocketRpcServer();
        rpcServer.publishService(config);
        rpcServer.start();

    }

}
