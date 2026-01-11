package com.my.server.service;

import cn.hutool.core.util.IdUtil;
import com.my.api.User;
import com.my.api.UserService;
import com.my.rpc.annotation.Limit;

public class UserServiceImpl implements UserService {

    @Limit(permitsPerSecond = 5.0, timeout = 0L)
    @Override
    public User getUser(Long id) {
        return User.builder()
                .id(id)
                .name("小鸭哥")
                .build();
    }
}
