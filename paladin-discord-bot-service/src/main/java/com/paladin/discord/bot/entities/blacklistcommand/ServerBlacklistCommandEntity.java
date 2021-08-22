package com.paladin.discord.bot.entities.blacklistcommand;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity(name = "ServerBlacklistCommandEntity")
@Table(name = "Server_Blacklist_Commands")
public class ServerBlacklistCommandEntity {

    @EmbeddedId
    private ServerBlacklistCommandEntityId serverBlacklistCommandEntityId;

    public ServerBlacklistCommandEntity() {
    }

    public ServerBlacklistCommandEntity(ServerBlacklistCommandEntityId serverBlacklistCommandEntityId) {
        this.serverBlacklistCommandEntityId = serverBlacklistCommandEntityId;
    }

    public ServerBlacklistCommandEntityId getBlacklistCommandEntityId() {
        return serverBlacklistCommandEntityId;
    }

    public void setBlacklistCommandEntityId(ServerBlacklistCommandEntityId serverBlacklistCommandEntityId) {
        this.serverBlacklistCommandEntityId = serverBlacklistCommandEntityId;
    }
}
