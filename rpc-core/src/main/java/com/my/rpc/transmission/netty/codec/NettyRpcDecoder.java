package com.my.rpc.transmission.netty.codec;

import cn.hutool.core.util.ArrayUtil;
import com.my.rpc.compress.Compress;
import com.my.rpc.compress.impl.GzipCompress;
import com.my.rpc.constant.RpcConstant;
import com.my.rpc.dto.RpcMsg;
import com.my.rpc.dto.RpcReq;
import com.my.rpc.dto.RpcResp;
import com.my.rpc.enums.CompressType;
import com.my.rpc.enums.MsgType;
import com.my.rpc.enums.SerializeType;
import com.my.rpc.enums.VersionType;
import com.my.rpc.exception.RpcException;
import com.my.rpc.factory.SingletonFactory;
import com.my.rpc.serialize.Serializer;
import com.my.rpc.serialize.impl.KryoSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * LengthFieldBasedFrameDecoder能解决粘包问题
 */
public class NettyRpcDecoder extends LengthFieldBasedFrameDecoder {

    public NettyRpcDecoder() {
        super(RpcConstant.REQ_MAX_LEN, 5, 4, -9, 0);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        return decodeFrame(frame);
    }

    private Object decodeFrame(ByteBuf frame) {
        readAndCheckMagicCode(frame);

        byte versionCode = frame.readByte();
        VersionType versionType = VersionType.from(versionCode);

        int msgLen = frame.readInt();

        byte msgTypeCode = frame.readByte();
        MsgType msgType = MsgType.from(msgTypeCode);

        byte serializerTypeCode = frame.readByte();
        SerializeType serializeType = SerializeType.from(serializerTypeCode);

        byte compressTypeCode = frame.readByte();
        CompressType compressType = CompressType.from(compressTypeCode);

        int reqId = frame.readInt();

        Object data = readData(frame, msgLen - RpcConstant.REQ_HEAD_LEN, msgType);

        return RpcMsg.builder()
                .id(reqId)
                .msgType(msgType)
                .version(versionType)
                .compressType(compressType)
                .serializeType(serializeType)
                .data(data)
                .build();
    }

    private void readAndCheckMagicCode(ByteBuf bytebuf) {
        byte[] magicBytes = new byte[RpcConstant.RPC_MAGIC_CODE.length];
        bytebuf.readBytes(magicBytes);
        if (!ArrayUtil.equals(magicBytes, RpcConstant.RPC_MAGIC_CODE)) {
            throw new RpcException("魔法值异常：" + new String(magicBytes));
        }
    }

    private Object readData(ByteBuf byteBuf, int dataLen, MsgType msgType) {
        if (msgType.isReq()) {
            return readData(byteBuf, dataLen, RpcReq.class);
        } else {
            return readData(byteBuf, dataLen, RpcResp.class);
        }
    }

    private <T> T readData(ByteBuf byteBuf, int dataLen, Class<T> clazz) {
        if (dataLen <= 0) {
            return null;
        }
        byte[] data = new byte[dataLen];
        byteBuf.readBytes(data);

        Compress compress = SingletonFactory.getInstance(GzipCompress.class);
        Serializer serializer = SingletonFactory.getInstance(KryoSerializer.class);

        data = compress.decompress(data);
        return serializer.deserializer(data, clazz);
    }

}
