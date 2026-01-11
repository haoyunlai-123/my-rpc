package com.my.rpc.breaker;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 状态模式
 * 一个接口方法对应一个熔断器对象
 */
public class CircuitBreaker {

    private State state = State.CLOSED;

    private final AtomicInteger failCount = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger total = new AtomicInteger(0);

    // 失败阈值
    private final int failThreshold;

    // 熔断器半开状态下的成功阈值
    private final double successRateInHaltOpen;
    // 熔断时间窗口
    private final long windowTime;

    private long lastFailTime = 0;

    public CircuitBreaker(int failThreshold, double successRateInHaltOpen, long windowTime) {
        this.failThreshold = failThreshold;
        this.successRateInHaltOpen = successRateInHaltOpen;
        this.windowTime = windowTime;
    }

    /**
     * 判断当前熔断器状态能否发请求
     * 在client端调用接口方法前执行，根据结果决定是否发请求出去
     * CLOSED -> OPEN -> HALF_OPEN ->
     * @return
     */
    public synchronized boolean canReq() {
        switch (state) {
            case CLOSED:
                return true;
            case OPEN:
                // 还在熔断状态
                if (System.currentTimeMillis() - lastFailTime <= windowTime) {
                    return false;
                }

                state = State.HALF_OPEN;
                resetCount();
                return true;
            case HALF_OPEN:
                total.incrementAndGet();
                return true;
            default:
                throw new IllegalArgumentException("熔断器状态异常");
        }
    }

    /**
     * 调用接口方法后根据是否成功选择执行
     */
    public synchronized void success() {
        if (state != State.HALF_OPEN) {
            resetCount();
            return;
        }

        successCount.incrementAndGet();
        // 此处逻辑有点小问题
        if (successCount.get() >= successRateInHaltOpen * total.get()) {
            state = State.CLOSED;
            resetCount();
        }

    }

    /**
     * 调用接口方法后根据是否成功选择执行
     */
    public synchronized void fail() {
        failCount.incrementAndGet();
        lastFailTime = System.currentTimeMillis();

        // 此处逻辑有问题
        if (state == State.HALF_OPEN) {
            state = State.OPEN;
            return;
        }

        if (failCount.get() >= failThreshold) {
            state = State.OPEN;
        }
    }

    private void resetCount() {
        failCount.set(0);
        successCount.set(0);
        total.set(0);
    }

    enum State {
        CLOSED,
        OPEN,
        HALF_OPEN,
    }

}
