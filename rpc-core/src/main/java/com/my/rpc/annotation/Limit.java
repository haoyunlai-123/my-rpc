package com.my.rpc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Limit {

    /**
     * 每秒支持多少请求访问 (每秒产生的令牌数)
     * @return
     */
    double permitsPerSecond();

    /**
     * 拿不到令牌的等待时间
     * @return
     */
    long timeout();

}
