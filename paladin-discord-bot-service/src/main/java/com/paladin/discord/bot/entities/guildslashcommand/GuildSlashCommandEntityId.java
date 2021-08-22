package com.paladin.discord.bot.entities.guildslashcommand;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class GuildSlashCommandEntityId implements Serializable {

    private String guildId;
    private String commandName;

    public GuildSlashCommandEntityId(String guildId, String commandName) {
        this.guildId = guildId;
        this.commandName = commandName;
    }

    public GuildSlashCommandEntityId() {
    }

    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }
}
