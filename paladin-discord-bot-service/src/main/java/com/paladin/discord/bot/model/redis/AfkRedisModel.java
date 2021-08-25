package com.paladin.discord.bot.model.redis;

import java.util.Objects;

public class AfkRedisModel {

    private String userId;
    private String message;
    private String creationTime;

    public AfkRedisModel() {
    }

    public AfkRedisModel(String userId, String message, String creationTime) {
        this.userId = userId;
        this.creationTime = creationTime;
        this.message = message;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AfkRedisModel that = (AfkRedisModel) o;
        return Objects.equals(userId, that.userId) && Objects.equals(creationTime, that.creationTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, creationTime);
    }
}
