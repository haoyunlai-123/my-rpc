package com.my.client.nettytest;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class NettyClient {
    public static void main(String[] args) throws InterruptedException {
        // 客户端引导器
        Bootstrap bootstrap = new Bootstrap();
        // 配置线程组
        bootstrap.group(new NioEventLoopGroup());
        // 指定 IO 类型为 NIO
        bootstrap.channel(NioSocketChannel.class);
        // 配置 IO 处理器
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                // 添加字符串解码器
                ch.pipeline().addLast(new StringDecoder());
                // 添加字符串编码器
                ch.pipeline().addLast(new StringEncoder());

                ch.pipeline().addLast(new MyClientHandler());
            }
        });
        // 建立连接
        Channel channel = bootstrap.connect("127.0.0.1", 8081).channel();


    }

    // 客户端处理器类
    public static class MyClientHandler extends SimpleChannelInboundHandler<String> {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            // 连接建立后向服务端发送消息每隔5秒发送一次
            // while (true) {
            //     ctx.channel().writeAndFlush("hello world..");
            //     TimeUnit.SECONDS.sleep(5);
            // }

            // 连接建立后向服务端发送一次消息
            String msg = "client msg 1";
            System.out.println("客户端发送消息: " + msg);
            ctx.channel().writeAndFlush(msg);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            System.out.println("客户端收到服务端消息: " + msg);
        }
    }
}