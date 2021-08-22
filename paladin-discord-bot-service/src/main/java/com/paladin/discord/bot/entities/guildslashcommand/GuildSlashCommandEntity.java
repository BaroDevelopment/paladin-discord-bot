package com.paladin.discord.bot.entities.guildslashcommand;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity(name = "GuildSlashCommandEntity")
@Table(name = "Slash_Commands")
public class GuildSlashCommandEntity {

    @EmbeddedId
    private GuildSlashCommandEntityId guildSlashCommandEntityId;

    public GuildSlashCommandEntity() {
    }

    public GuildSlashCommandEntity(GuildSlashCommandEntityId guildSlashCommandEntityId) {
        this.guildSlashCommandEntityId = guildSlashCommandEntityId;
    }

    public GuildSlashCommandEntityId getGuildSlashCommandEntityId() {
        return guildSlashCommandEntityId;
    }

    public void setGuildSlashCommandEntityId(GuildSlashCommandEntityId guildSlashCommandEntityId) {
        this.guildSlashCommandEntityId = guildSlashCommandEntityId;
    }
}