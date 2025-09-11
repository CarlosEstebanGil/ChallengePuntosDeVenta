package com.carlos.challenge.infrastructure.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

@Configuration
@Profile("redis")
public class RedisConfig {

    @Bean
    public RedissonClient redissonClient(
            @Value("${redis.url:redis://127.0.0.1:6379}") String url) {
        Config cfg = new Config();
        cfg.setCodec(new JsonJacksonCodec());
        cfg.useSingleServer().setAddress(url);
        return Redisson.create(cfg);
    }
}
