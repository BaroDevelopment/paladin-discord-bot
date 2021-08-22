package com.paladin.discord.bot.repository.redis;

import com.paladin.discord.bot.model.redis.AfkRedisModel;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Repository
public class RedisStringRepository {

    private final ReactiveRedisTemplate<String, AfkRedisModel> redisTemplate;
    private final ReactiveValueOperations<String, AfkRedisModel> reactiveValueOps;

    public RedisStringRepository(ReactiveRedisTemplate<String, AfkRedisModel> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.reactiveValueOps = redisTemplate.opsForValue();
    }

    public Mono<AfkRedisModel> findOne(String key) {
        return reactiveValueOps.get(key);
    }

    public void save(String key, AfkRedisModel model, long duration) {
        Mono<Boolean> result = reactiveValueOps.set(key, model, Duration.ofMinutes(duration));
        result.subscribe();
    }

    public Mono<Boolean> delete(String key) {
        return reactiveValueOps.delete(key);
    }
}