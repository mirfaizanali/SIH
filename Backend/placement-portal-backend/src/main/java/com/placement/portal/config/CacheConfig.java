package com.placement.portal.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Caffeine-based cache configuration.
 *
 * <table border="1">
 *   <caption>Cache settings</caption>
 *   <tr><th>Cache name</th><th>Expire after write</th><th>Max size</th></tr>
 *   <tr><td>active_jobs</td><td>5 min</td><td>500</td></tr>
 *   <tr><td>student_profiles</td><td>10 min</td><td>1000</td></tr>
 *   <tr><td>analytics_dashboard</td><td>15 min</td><td>100</td></tr>
 * </table>
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();

        manager.setCaches(List.of(
                buildCache("active_jobs", 5, 500),
                buildCache("student_profiles", 10, 1_000),
                buildCache("analytics_dashboard", 15, 100)
        ));

        return manager;
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private CaffeineCache buildCache(String name, long expireAfterWriteMinutes, long maximumSize) {
        return new CaffeineCache(
                name,
                Caffeine.newBuilder()
                        .expireAfterWrite(expireAfterWriteMinutes, TimeUnit.MINUTES)
                        .maximumSize(maximumSize)
                        .recordStats()
                        .build()
        );
    }
}
