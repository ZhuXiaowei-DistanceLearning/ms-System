package com.zxw.lock;

import java.util.concurrent.TimeUnit;

/**
 * @author zxw
 * @date 2020/7/2 11:39
 */
public interface DistributedLocker {
    void lock(String lockKey);

    void unlock(String lockKey);

    void lock(String lockKey, int timeout);

    void lock(String lockKey, TimeUnit unit, int timeout);
}
