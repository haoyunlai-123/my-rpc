package com.my.server.service;

import cn.hutool.core.util.IdUtil;
import com.my.api.User;
import com.my.api.UserService;

public class UserServiceImpl implements UserService {

    @Override
    public User getUser(Long id) {
        return User.builder()
                .id(id)
                .name("小鸭哥")
                .build();
    }
}
