package com.serwisspolecznosciowy.Application.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@EnableScheduling
public class CacheConfig {

    @Autowired
    CacheManager cacheManager;

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager simpleCacheManager = new SimpleCacheManager();
        List<Cache> caches = new ArrayList<>();
        caches.add(new ConcurrentMapCache("PostsWithComments"));
        caches.add(new ConcurrentMapCache("AllPostsDto"));
        caches.add(new ConcurrentMapCache("AllComments"));
        caches.add(new ConcurrentMapCache("AllCommentsDto"));
        simpleCacheManager.setCaches(caches);
        return simpleCacheManager;
    }

    public void evictAllCaches() {
        cacheManager.getCacheNames().stream()
                .forEach(cacheName -> cacheManager.getCache(cacheName).clear());
    }

    @Scheduled(fixedRate = 30, timeUnit=TimeUnit.SECONDS)
    public void evictAllCachesAtIntervals() {
        evictAllCaches();
    }

}
