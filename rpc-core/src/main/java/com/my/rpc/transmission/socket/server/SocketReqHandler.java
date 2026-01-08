package com.my.rpc.transmission.socket.server;

import com.my.rpc.dto.RpcReq;
import com.my.rpc.dto.RpcResp;
import com.my.rpc.handler.RpcReqHandler;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

@Slf4j
@AllArgsConstructor
public class SocketReqHandler implements Runnable {

    private final Socket socket;
    private final RpcReqHandler rpcReqHandler;

    // 自动捕获异常的注解
    @SneakyThrows
    @Override
    public void run() {
        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
        RpcReq req = (RpcReq) inputStream.readObject();
        System.out.println(req);

        // 假装调用了接口实现类中的方法
//                String data = "13as4d56as";
        Object data = rpcReqHandler.invoke(req);
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        RpcResp<Object> res = RpcResp.success(req.getReqId(), data);
        outputStream.writeObject(res);
        outputStream.flush();
    }
}
