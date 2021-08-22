package com.paladin.discord.bot.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "VerifiedServerEntity")
@Table(name = "Verified_Servers")
public class VerifiedServerEntity {

    @Id
    private String guildId;

    public VerifiedServerEntity() {
    }

    public VerifiedServerEntity(String guildId) {
        this.guildId = guildId;
    }

    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }
}
