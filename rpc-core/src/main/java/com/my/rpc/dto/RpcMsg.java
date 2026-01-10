package com.my.rpc.dto;

import com.my.rpc.enums.CompressType;
import com.my.rpc.enums.MsgType;
import com.my.rpc.enums.SerializeType;
import com.my.rpc.enums.VersionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RpcMsg implements Serializable {
    private static final long serialVersionUID = 1L;

    private VersionType version;
    private MsgType msgType;
    private SerializeType serializeType;
    private CompressType compressType;
    private Object data;
    private Integer id;

}
