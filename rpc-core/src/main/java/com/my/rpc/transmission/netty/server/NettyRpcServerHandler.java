package com.my.rpc.transmission.netty.server;

import com.my.rpc.dto.RpcMsg;
import com.my.rpc.dto.RpcReq;
import com.my.rpc.dto.RpcResp;
import com.my.rpc.enums.CompressType;
import com.my.rpc.enums.MsgType;
import com.my.rpc.enums.SerializeType;
import com.my.rpc.enums.VersionType;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyRpcServerHandler extends SimpleChannelInboundHandler<RpcMsg> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMsg rpcMsg) throws Exception {
        log.debug("接收到客户端的请求：{}", rpcMsg);

        RpcReq rpcReq = (RpcReq) rpcMsg.getData();

        RpcResp<String> rpcResp = RpcResp.success(rpcReq.getReqId(), "模拟响应数据");

        RpcMsg msg = RpcMsg.builder()
                .id(rpcMsg.getId())
                .version(VersionType.VERSION1)
                .msgType(MsgType.RPC_RESP)
                .compressType(CompressType.GZIP)
                .serializeType(SerializeType.KRYO)
                .data(rpcReq)
                .build();

        ctx.channel().writeAndFlush(msg).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("服务端异常", cause);
        ctx.close();
    }
}
