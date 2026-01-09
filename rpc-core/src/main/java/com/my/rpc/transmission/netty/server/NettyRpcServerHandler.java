package com.my.rpc.transmission.netty.server;

import com.my.rpc.dto.RpcReq;
import com.my.rpc.dto.RpcResp;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyRpcServerHandler extends SimpleChannelInboundHandler<String> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String req) throws Exception {
        log.debug("接收到客户端的请求：{}", req);

//        RpcResp<String> rpcResp = RpcResp.success(req, "模拟响应数据");
        ctx.channel().writeAndFlush("模拟响应数据").addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("服务端异常", cause);
        ctx.close();
    }
}
