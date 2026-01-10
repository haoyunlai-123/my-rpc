package com.my.rpc.transmission.netty.server;

import com.my.rpc.dto.RpcMsg;
import com.my.rpc.dto.RpcReq;
import com.my.rpc.dto.RpcResp;
import com.my.rpc.enums.CompressType;
import com.my.rpc.enums.MsgType;
import com.my.rpc.enums.SerializeType;
import com.my.rpc.enums.VersionType;
import com.my.rpc.factory.SingletonFactory;
import com.my.rpc.handler.RpcReqHandler;
import com.my.rpc.provider.ServiceProvider;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyRpcServerHandler extends SimpleChannelInboundHandler<RpcMsg> {

    private final RpcReqHandler rpcReqHandler;

    // 也可以在RpcReqHandler中添加一个空参构造，调单例的serviceProvider即可
    public NettyRpcServerHandler(ServiceProvider serviceProvider) {
        this.rpcReqHandler = new RpcReqHandler(serviceProvider);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMsg rpcMsg) throws Exception {
        log.debug("接收到客户端的请求：{}", rpcMsg);

        MsgType msgType;
        Object data;
        if (rpcMsg.getMsgType().isHeartBeat()) {
            msgType = MsgType.HEARTBEAT_RESP;
            data = null;
        } else {
            msgType = MsgType.RPC_RESP;
            RpcReq rpcReq = (RpcReq) rpcMsg.getData();
            data = handlerRpcReq(rpcReq);
        }


        RpcMsg msg = RpcMsg.builder()
                .id(rpcMsg.getId())
                .version(VersionType.VERSION1)
                .msgType(msgType)
                .compressType(CompressType.GZIP)
                .serializeType(SerializeType.KRYO)
                .data(data)
                .build();

        ctx.channel().writeAndFlush(msg).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("服务端异常", cause);
        ctx.close();
    }

    private RpcResp<?> handlerRpcReq(RpcReq req) {
        try {
            Object object = rpcReqHandler.invoke(req);
            return RpcResp.success(req.getReqId(), object);
        } catch (Exception e) {
            log.error("调用失败", e);
            return RpcResp.fail(req.getReqId(), e.getMessage());
        }
    }
}
