package com.my.rpc.transmission.netty.client;

import com.my.rpc.constant.RpcConstant;
import com.my.rpc.dto.RpcResp;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyRpcClientHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String rpcResp) throws Exception {
        log.info("收到服务端的数据: {}", rpcResp);

        AttributeKey<String> key = AttributeKey.valueOf(RpcConstant.NETTY_PRC_KEY);
        ctx.channel().attr(key).set(rpcResp);
        ctx.channel().close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("服务端发生异常", cause);
        ctx.close();
    }
}
