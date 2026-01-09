package com.my.rpc.transmission.netty.codec;

import com.my.rpc.compress.Compress;
import com.my.rpc.compress.impl.GzipCompress;
import com.my.rpc.constant.RpcConstant;
import com.my.rpc.dto.RpcMsg;
import com.my.rpc.enums.SerializeType;
import com.my.rpc.factory.SingletonFactory;
import com.my.rpc.serialize.Serializer;
import com.my.rpc.serialize.impl.KryoSerializer;
import com.sun.deploy.xml.XMLAttributeBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class NettyRpcEncoder extends MessageToByteEncoder<RpcMsg> {


    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMsg rpcMsg, ByteBuf byteBuf) throws Exception {
        byteBuf.writeBytes(RpcConstant.RPC_MAGIC_CODE);
        byte version = rpcMsg.getVersion().getCode();
        byteBuf.writeByte(version);

        // bytebuff往右移动四位，给报文总长度留出空间
        byteBuf.writerIndex(byteBuf.writerIndex() + 4);

        byteBuf.writeByte(rpcMsg.getMsgType().getCode());
        byteBuf.writeByte(rpcMsg.getCompressType().getCode());
        byteBuf.writeByte(rpcMsg.getSerializeType().getCode());

        byteBuf.writeInt(rpcMsg.getId());

        int msgLen = RpcConstant.REQ_HEAD_LEN;
        if (! rpcMsg.getMsgType().isHeartBeat()
            && ! Objects.isNull(rpcMsg.getData())) {
            byte[] data = data2Bytes(rpcMsg);
            byteBuf.writeBytes(data);
            msgLen += data.length;
        }

        int curIdx = byteBuf.writerIndex();
        byteBuf.writerIndex(curIdx - msgLen + RpcConstant.RPC_MAGIC_CODE.length + 1);
        byteBuf.writeInt(msgLen);
        byteBuf.writerIndex(curIdx);
    }

    public byte[] data2Bytes(RpcMsg rpcMsg) {
        // TODO 获取序列化和压缩类型

        Serializer serializer = SingletonFactory.getInstance(KryoSerializer.class);
        Compress compress = SingletonFactory.getInstance(GzipCompress.class);

        byte[] data = serializer.serialize(rpcMsg.getData());
        data = compress.compress(data);
        return data;
    }
}
