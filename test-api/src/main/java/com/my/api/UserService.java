package com.my.api;

import com.my.rpc.annotation.Retry;

public interface UserService {

    @Retry(maxAttempts = 4, delay = 5000)
    User getUser(Long id);
}
