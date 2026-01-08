package com.my.rpc.transmission.socket.client;

import com.my.rpc.dto.RpcReq;
import com.my.rpc.dto.RpcResp;
import com.my.rpc.factory.SingletonFactory;
import com.my.rpc.registry.ServiceDiscovery;
import com.my.rpc.registry.impl.ZkServiceDiscovery;
import com.my.rpc.transmission.RpcClient;
import lombok.extern.slf4j.Slf4j;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

@Slf4j
public class SocketClient implements RpcClient {

    private final ServiceDiscovery serviceDiscovery;

    public SocketClient() {
        this(SingletonFactory.getInstance(ZkServiceDiscovery.class));
    }

    public SocketClient(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    @Override
    public RpcResp<?> sendReq(RpcReq req) {

        InetSocketAddress address = serviceDiscovery.lookupService(req);

        try (Socket socket = new Socket(address.getAddress(), address.getPort())) {
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(req);
            outputStream.flush();

            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            Object o = inputStream.readObject();
            return (RpcResp<?>) o;
        } catch (Exception e) {
            log.error("发送rpc请求失败", e);
        }

        return null;
    }
}
