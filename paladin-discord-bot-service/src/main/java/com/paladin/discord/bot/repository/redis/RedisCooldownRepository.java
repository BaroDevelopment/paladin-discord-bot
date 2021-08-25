package com.paladin.discord.bot.repository.redis;

import com.paladin.discord.bot.component.commands.ICommand;
import com.paladin.discord.bot.component.commands.ISlashCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@Repository
public class RedisCooldownRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCooldownRepository.class);
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ReactiveValueOperations<String, String> reactiveValueOps;
    private static final String COOLDOWN_FORMAT_KEY = "cooldown:%s:%s";

    public RedisCooldownRepository(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.reactiveValueOps = redisTemplate.opsForValue();
    }

    // Return 0 if user has 0 cooldown and return the amount of seconds left otherwise
    public long hasCooldown(ICommand cmd, String userId) {
        String redisValue = reactiveValueOps.get(String.format(COOLDOWN_FORMAT_KEY, cmd.getName(), userId)).block();
        if (redisValue == null) {
            // save to redis
            save(cmd.getName(), userId, OffsetDateTime.now(ZoneOffset.UTC).toString(), cmd.getCooldown());
            return 0;
        }

        OffsetDateTime oldTime = OffsetDateTime.parse(redisValue);
        OffsetDateTime newTime = OffsetDateTime.now(ZoneOffset.UTC);

        return ChronoUnit.SECONDS.between(oldTime, newTime);
    }

    public long hasCooldown(ISlashCommand cmd, String userId) {
        String redisValue = reactiveValueOps.get(String.format(COOLDOWN_FORMAT_KEY, cmd.getName(), userId)).block();
        if (redisValue == null) {
            // save to redis
            save(cmd.getName(), userId, OffsetDateTime.now(ZoneOffset.UTC).toString(), cmd.getCooldown());
            return 0;
        }

        OffsetDateTime oldTime = OffsetDateTime.parse(redisValue);
        OffsetDateTime newTime = OffsetDateTime.now(ZoneOffset.UTC);

        return ChronoUnit.SECONDS.between(oldTime, newTime);
    }

    public void save(String commandName, String userId, String dateTimeOffset, long duration) {
        if (duration <= 0) return;

        Mono<Boolean> result = reactiveValueOps.set(String.format(COOLDOWN_FORMAT_KEY, commandName, userId),
                dateTimeOffset, Duration.ofSeconds(duration));
        result.subscribe();
    }

    public Mono<Boolean> delete(String key) {
        return reactiveValueOps.delete(key);
    }
}