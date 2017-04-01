package com.janita.sso.cache;


import com.janita.sso.cache.strategy.CacheStrategy;

import java.util.TimerTask;

/**
 * 缓存清除任务
 */
public class CacheEvictTask extends TimerTask {
    private final CacheStrategy cacheStrategy; //缓存策略
    private final Object key; //要清除的键

    public CacheEvictTask(CacheStrategy cacheStrategy, Object key) {
        this.cacheStrategy = cacheStrategy;
        this.key = key;
    }

    @Override
    public void run() {
        cacheStrategy.evict(key);
    }
}
