package com.my.server.nettytest;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class NettyServer {
    public static void main(String[] args) {
        // 创建服务端启动引导器
        ServerBootstrap bootstrap = new ServerBootstrap();

        // 配置线程模型
        NioEventLoopGroup bossEventLoopGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerEventLoopGroup = new NioEventLoopGroup();
        bootstrap.group(bossEventLoopGroup, workerEventLoopGroup);

        // 指定服务端的 IO 模型
        bootstrap.channel(NioServerSocketChannel.class);

        // 定义处理器 Handler
        bootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                // 添加字符串解码器
                ch.pipeline().addLast(new StringDecoder());
                // 添加字符串编码器
                ch.pipeline().addLast(new StringEncoder());

                // 添加自定义业务逻辑处理
                ch.pipeline().addLast(new MyServerHandler());
            }
        });

        // 绑定 8081 端口
        bootstrap.bind(8081);
    }

    public static class MyServerHandler extends SimpleChannelInboundHandler<String> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            System.out.println("服务器收到消息: " + msg);

            String sendMsg = "server msg 1";
            System.out.println("服务器发送消息: " + sendMsg);
            ctx.channel().writeAndFlush(sendMsg);
        }
    }
}