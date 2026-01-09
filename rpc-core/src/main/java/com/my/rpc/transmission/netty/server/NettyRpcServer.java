package com.my.rpc.transmission.netty.server;

import com.my.rpc.config.RpcServiceConfig;
import com.my.rpc.constant.RpcConstant;
import com.my.rpc.transmission.RpcClient;
import com.my.rpc.transmission.RpcServer;
import com.my.rpc.transmission.netty.codec.NettyRpcDecoder;
import com.my.rpc.transmission.netty.codec.NettyRpcEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyRpcServer implements RpcServer {

    @Override
    public void start() {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline().addLast(new NettyRpcDecoder());
                            channel.pipeline().addLast(new NettyRpcEncoder());
                            channel.pipeline().addLast(new NettyRpcServerHandler());
                        }
                    });

            ChannelFuture channelFuture = bootstrap.bind(RpcConstant.SERVER_PORT).sync();
            log.info("netty rpc server已启动，端口为：{}", RpcConstant.SERVER_PORT);
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("服务端异常", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }

    @Override
    public void publishService(RpcServiceConfig config) {

    }
}
