package edu.iu.uits.lms.canvas.config;

import edu.iu.uits.lms.canvas.utils.CacheConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import java.time.Duration;

@Profile("redis-cache")
@Configuration
@EnableCaching
@Slf4j
public class CanvasRedisCacheConfig {

    @Autowired
    private CanvasConfiguration canvasConfiguration;

    @Autowired
    private JedisConnectionFactory redisConnectionFactory;

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        final int ttl = 300;
        return RedisCacheConfiguration.defaultCacheConfig()
              .entryTtl(Duration.ofSeconds(ttl))
              .disableCachingNullValues()
              .prefixCacheNameWith(canvasConfiguration.getEnv() + "-canvasservices");
    }

    @Bean
    public RedisCacheConfiguration cacheLongConfiguration() {
        final int ttl = 3600;
        return RedisCacheConfiguration.defaultCacheConfig()
              .entryTtl(Duration.ofSeconds(ttl))
              .disableCachingNullValues()
              .prefixCacheNameWith(canvasConfiguration.getEnv() + "-canvasservices");
    }

    @Bean(name = "CanvasServicesCacheManager")
    public CacheManager cacheManager() {
        log.debug("cacheManager()");
        log.debug("Redis hostname: {}", redisConnectionFactory.getHostName());
        return RedisCacheManager.builder(redisConnectionFactory)
              .withCacheConfiguration(CacheConstants.ENROLLMENT_TERMS_CACHE_NAME, cacheConfiguration())
              .withCacheConfiguration(CacheConstants.PARENT_ACCOUNTS_CACHE_NAME, cacheLongConfiguration())
              .build();
    }
}
