package com.paladin.discord.bot.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "GlobalBanEntity")
@Table(name = "Global_Bans")
public class GlobalBanEntity {

    public GlobalBanEntity() {
    }

    @Id
    private String userId;

    public GlobalBanEntity(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
