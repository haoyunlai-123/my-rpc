package com.my.rpc.transmission.netty.client;

import com.my.rpc.constant.RpcConstant;
import com.my.rpc.dto.RpcReq;
import com.my.rpc.dto.RpcResp;
import com.my.rpc.transmission.RpcClient;
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

@Slf4j
public class NettyClient implements RpcClient {

    private static final Bootstrap BOOTSTRAP;
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;

    static {
        BOOTSTRAP = new Bootstrap();
        BOOTSTRAP.group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, DEFAULT_CONNECT_TIMEOUT)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        channel.pipeline().addLast(new StringDecoder());
                        channel.pipeline().addLast(new StringEncoder());
                        channel.pipeline().addLast(new NettyRpcClientHandler());
                    }
                });
    }

    @SneakyThrows
    @Override
    public RpcResp<?> sendReq(RpcReq req) {

        // 异步连接server端
        ChannelFuture channelFuture = BOOTSTRAP.connect("127.0.0.1", 8888).sync();

        log.info("netty rpc client连接已建立");

        Channel channel = channelFuture.channel();

        String interfaceName = req.getInterfaceName();

        channel.writeAndFlush(interfaceName).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        channel.closeFuture().sync();

        // channel中map的key
        AttributeKey<String> key = AttributeKey.valueOf(RpcConstant.NETTY_PRC_KEY);

        String s = channel.attr(key).get();
        System.out.println(s);
        return null;
    }
}
