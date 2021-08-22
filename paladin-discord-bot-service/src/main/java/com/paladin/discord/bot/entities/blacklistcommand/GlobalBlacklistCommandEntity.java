package com.paladin.discord.bot.entities.blacklistcommand;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "GlobalBlacklistCommandEntity")
@Table(name = "Global_Blacklist_User")
public class GlobalBlacklistCommandEntity {

    @Id
    private String userId;

    public GlobalBlacklistCommandEntity() {
    }

    public GlobalBlacklistCommandEntity(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
