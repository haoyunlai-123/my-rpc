package com.my.rpc.proxy;

import cn.hutool.core.util.IdUtil;
import com.my.rpc.config.RpcServiceConfig;
import com.my.rpc.dto.RpcReq;
import com.my.rpc.dto.RpcResp;
import com.my.rpc.enums.RpcRespStatus;
import com.my.rpc.exception.RpcException;
import com.my.rpc.transmission.RpcClient;
import com.my.rpc.transmission.socket.client.SocketClient;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

@Slf4j
public class RpcClientProxy implements InvocationHandler {

    private final RpcClient rpcClient;
    private final RpcServiceConfig rpcServiceConfig;

    public RpcClientProxy(RpcClient rpcClient) {
        this(rpcClient, new RpcServiceConfig());
    }

    public RpcClientProxy(RpcClient rpcClient, RpcServiceConfig rpcServiceConfig) {
        this.rpcClient = rpcClient;
        this.rpcServiceConfig = rpcServiceConfig;
    }

//    public <T> T getProxy() {
//        Class<?>[] interfaces = rpcClient.getClass().getInterfaces();
//        if (interfaces.length == 0) {
//            throw new IllegalArgumentException("Target class " + rpcClient.getClass().getName() +
//                    " does not implement any interfaces");
//        }
//
//        Object proxy = Proxy.newProxyInstance(
//                rpcClient.getClass().getClassLoader(),
//                interfaces,
//                this
//        );
//
//        // 通过泛型类型推断来确保类型安全
//        return (T) proxy;
//    }

    // 抑制编译器警告
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class[]{clazz},
                this
        );
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        RpcReq req = RpcReq.builder()
                .reqId(IdUtil.fastSimpleUUID())
                // 注意，这里拿到的是接口的全类名
                .interfaceName(method.getDeclaringClass().getCanonicalName())
                .methodName(method.getName())
                .params(args)
                .paramTypes(method.getParameterTypes())
                .version(rpcServiceConfig.getVersion())
                .group(rpcServiceConfig.getGroup())
                .build();

        RpcResp<?> rpcResp = rpcClient.sendReq(req);

        check(req, rpcResp);
        return rpcResp.getData();
    }

    private void check(RpcReq req, RpcResp<?> rpcResp) {
        if (Objects.isNull(rpcResp)) {
            throw new RpcException("rpcResp为空");
        }

        // 有非空校验
        if (! Objects.equals(req.getReqId(), rpcResp.getReqId())) {
            throw new RpcException("请求和响应的id不一致");
        }

        if (RpcRespStatus.isFailed(rpcResp.getCode())) {
            throw new RpcException("响应值为失败：" + rpcResp.getMsg());
        }
    }
}
