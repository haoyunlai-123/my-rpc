package com.my.rpc.transmission.socket.server;

import com.my.rpc.config.RpcServiceConfig;
import com.my.rpc.constant.RpcConstant;
import com.my.rpc.dto.RpcReq;
import com.my.rpc.dto.RpcResp;
import com.my.rpc.factory.SingletonFactory;
import com.my.rpc.handler.RpcReqHandler;
import com.my.rpc.provider.ServiceProvider;
import com.my.rpc.provider.impl.SimpleServiceProvider;
import com.my.rpc.provider.impl.ZkServiceProvider;
import com.my.rpc.transmission.RpcServer;
import com.my.rpc.util.ThreadPoolUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import sun.misc.Request;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

@Slf4j
public class SocketRpcServer implements RpcServer {

    private final int port;
    private final RpcReqHandler rpcReqHandler;
    private final ServiceProvider serviceProvider;
    private final ExecutorService executor;

    public SocketRpcServer() {
        this(RpcConstant.SERVER_PORT);
    }

    public SocketRpcServer(int port) {
        this(port, SingletonFactory.getInstance(ZkServiceProvider.class));
    }

    public SocketRpcServer(int port, ServiceProvider serviceProvider) {
        this.port = port;
        this.serviceProvider = serviceProvider;
        this.rpcReqHandler = new RpcReqHandler(serviceProvider);
        this.executor = ThreadPoolUtils.createIoIntensiveThreadPool("socket-rpc-server-");
    }

    @Override
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            log.info("Server started on port: {}", port);

            Socket socket;
            while ((socket = serverSocket.accept()) != null) {
                executor.submit(new SocketReqHandler(socket, rpcReqHandler));
            }
        }  catch (Exception e) {
            log.error("服务端异常", e);
        }
    }

    @Override
    public void publishService(RpcServiceConfig config) {
        serviceProvider.publishService(config);
    }

}
