package com.my.client;

import com.my.api.User;
import com.my.api.UserService;
import com.my.client.utils.ProxyUtils;
import com.my.rpc.dto.RpcReq;
import com.my.rpc.dto.RpcResp;
import com.my.rpc.proxy.RpcClientProxy;
import com.my.rpc.transmission.RpcClient;
import com.my.rpc.transmission.socket.client.SocketClient;

public class Main {
    public static void main1(String[] args) {
        /*UserService userService = new UserServiceImpl();
        User user = userService.getUser(1L);
        System.out.println(user);*/

        /*RpcClient client = new RpcClient() {
            @Override
            public RpcResp<?> sendReq(RpcReq req) {
                return null;
            }
        };*/

        /*RpcClient client = new SocketClient("127.0.0.1", 8888);

        RpcReq req = RpcReq.builder()
                .reqId("114514")
                .interfaceName("com.my.api.UserService")
                .methodName("getUser")
                .params(new Object[]{1L})
                .paramTypes(new Class[]{Long.class})
                .build();

        RpcResp<?> rpcResp = client.sendReq(req);
//        User user = (User) rpcResp.getData();
        System.out.println(rpcResp.getData());*/

    }

    public static void main(String[] args) {
//        UserService userService = getProxy(UserService.class);
        UserService userService = ProxyUtils.getProxy(UserService.class);
        User user = userService.getUser(1L);
        System.out.println(user);
    }

    private static <T> T getProxy(Class<T> clazz) {
        SocketClient client = new SocketClient();
        RpcClientProxy proxy = new RpcClientProxy(client);
        return proxy.getProxy(clazz);
    }
}
