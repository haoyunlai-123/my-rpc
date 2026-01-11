package com.my.rpc.transmission.netty.client;

import com.my.rpc.constant.RpcConstant;
import com.my.rpc.dto.RpcMsg;
import com.my.rpc.dto.RpcReq;
import com.my.rpc.dto.RpcResp;
import com.my.rpc.enums.CompressType;
import com.my.rpc.enums.MsgType;
import com.my.rpc.enums.SerializeType;
import com.my.rpc.enums.VersionType;
import com.my.rpc.factory.SingletonFactory;
import com.my.rpc.registry.ServiceDiscovery;
import com.my.rpc.registry.impl.ZkServiceDiscovery;
import com.my.rpc.transmission.RpcClient;
import com.my.rpc.transmission.netty.codec.NettyRpcDecoder;
import com.my.rpc.transmission.netty.codec.NettyRpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.CompleteFuture;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class NettyClient implements RpcClient {

    private static final Bootstrap BOOTSTRAP;
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;

    private final ServiceDiscovery serviceDiscovery;

    private final ChannelPool channelPool;

    public NettyClient() {
        this(SingletonFactory.getInstance(ZkServiceDiscovery.class));
    }

    public NettyClient(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
        this.channelPool = SingletonFactory.getInstance(ChannelPool.class);
    }

    static {
        BOOTSTRAP = new Bootstrap();
        BOOTSTRAP.group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, DEFAULT_CONNECT_TIMEOUT)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        channel.pipeline().addLast(new IdleStateHandler(0, 5, 0,
                                TimeUnit.SECONDS));
                        channel.pipeline().addLast(new NettyRpcDecoder());
                        channel.pipeline().addLast(new NettyRpcEncoder());
                        channel.pipeline().addLast(new NettyRpcClientHandler());
                    }
                });
    }

    @SneakyThrows
    @Override
    public Future<RpcResp<?>> sendReq(RpcReq req) {

        // 创建异步任务
        CompletableFuture<RpcResp<?>> future = new CompletableFuture<>();
        // 占位
        UnprocessedRpcReq.put(req.getReqId(), future);

        InetSocketAddress address = serviceDiscovery.lookupService(req);

        // 异步连接server端
        Channel channel = channelPool.get(address, () -> connect(address));

        log.info("netty rpc client连接已建立, 连接到： {}", address);


        RpcMsg rpcMsg = RpcMsg.builder()
                .version(VersionType.VERSION1)
                .serializeType(SerializeType.KRYO)
                .compressType(CompressType.GZIP)
                .msgType(MsgType.RPC_REQ)
                .data(req)
                .build();

        channel.writeAndFlush(rpcMsg)
                .addListener((ChannelFutureListener) listener -> {
            if (! listener.isSuccess()) {
                listener.channel().close();
                future.completeExceptionally(listener.cause());
            }
        });

        /*// 阻塞等待
        channel.closeFuture();



        // channel中map的key
        // 此处泛型为map的值的类型
        AttributeKey<RpcResp<?>> key = AttributeKey.valueOf(RpcConstant.NETTY_PRC_KEY);*/

        // CompletableFuture可以用阻塞队列实现
        return future;
    }

    private Channel connect(InetSocketAddress address) {
        try {
            return BOOTSTRAP.connect(address).sync().channel();
        } catch (InterruptedException e) {
            log.error("连接到远程服务器失败", e);
            throw new RuntimeException(e);
        }
    }
}
