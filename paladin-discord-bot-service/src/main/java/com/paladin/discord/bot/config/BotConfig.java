package com.paladin.discord.bot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class BotConfig {

    private final String token;
    private final String activity;
    private final String defaultPrefix;
    private final String defaultAfkMessage;
    private final boolean serverVerificationEnabled;
    private final int redisCacheDurationMinutes;
    private final int shardCount;
    private final List<String> botOwnerIds;

    public BotConfig(
            @Value("${token}") String token,
            @Value("${activity}") String activity,
            @Value("${defaultPrefix}") String defaultPrefix,
            @Value("${defaultAfkMessage}") String defaultAfkMessage,
            @Value("${serverVerificationEnabled}") boolean serverVerificationEnabled,
            @Value("${shardCount}") int shardCount,
            @Value("${redis.message.cache.duration.minutes}") int redisCacheDurationMinutes,
            @Value("${ownerIds}") List<String> botOwnerIds) {
        this.token = token;
        this.activity = activity;
        this.defaultPrefix = defaultPrefix;
        this.defaultAfkMessage = defaultAfkMessage;
        this.serverVerificationEnabled = serverVerificationEnabled;
        this.shardCount = shardCount;
        this.redisCacheDurationMinutes = redisCacheDurationMinutes;
        this.botOwnerIds = botOwnerIds;
    }

    public String getToken() {
        return token;
    }

    public String getActivity() {
        return activity;
    }

    public String getDefaultPrefix() {
        return defaultPrefix;
    }

    public int getShardCount() {
        return shardCount;
    }

    public List<String> getBotOwnerIds() {
        return botOwnerIds;
    }

    public String getDefaultAfkMessage() {
        return defaultAfkMessage;
    }

    public int getRedisCacheDurationMinutes() {
        return redisCacheDurationMinutes;
    }

    public boolean isServerVerificationEnabled() {
        return serverVerificationEnabled;
    }
}
