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
import io.netty.util.AttributeKey;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class NettyClient implements RpcClient {

    // 防止多个线程同时自增导致线程安全问题，例如：线程1读取后加一再赋值，线程2也读取后加一再赋值，加了两次但值只加了一
    private static final AtomicInteger ID_GEN = new AtomicInteger(0);

    private static final Bootstrap BOOTSTRAP;
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;

    private final ServiceDiscovery serviceDiscovery;

    public NettyClient() {
        this(SingletonFactory.getInstance(ZkServiceDiscovery.class));
    }

    public NettyClient(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
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
                        channel.pipeline().addLast(new NettyRpcDecoder());
                        channel.pipeline().addLast(new NettyRpcEncoder());
                        channel.pipeline().addLast(new NettyRpcClientHandler());
                    }
                });
    }

    @SneakyThrows
    @Override
    public RpcResp<?> sendReq(RpcReq req) {

        InetSocketAddress address = serviceDiscovery.lookupService(req);

        // 异步连接server端
        ChannelFuture channelFuture = BOOTSTRAP.connect(address).sync();

        log.info("netty rpc client连接已建立, 连接到： {}", address);

        Channel channel = channelFuture.channel();

        RpcMsg rpcMsg = RpcMsg.builder()
                .id(ID_GEN.getAndIncrement())
                .version(VersionType.VERSION1)
                .serializeType(SerializeType.KRYO)
                .compressType(CompressType.GZIP)
                .msgType(MsgType.RPC_REQ)
                .data(req)
                .build();

        channel.writeAndFlush(rpcMsg).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);

        // 阻塞等待
        channel.closeFuture().sync();

        // channel中map的key
        // 此处泛型为map的值的类型
        AttributeKey<RpcResp<?>> key = AttributeKey.valueOf(RpcConstant.NETTY_PRC_KEY);

        return channel.attr(key).get();
    }
}
