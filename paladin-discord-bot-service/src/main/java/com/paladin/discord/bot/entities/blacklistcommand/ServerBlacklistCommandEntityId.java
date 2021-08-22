package com.paladin.discord.bot.entities.blacklistcommand;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class ServerBlacklistCommandEntityId implements Serializable {

    private String serverId;
    private String userId;

    public ServerBlacklistCommandEntityId() {
    }

    public ServerBlacklistCommandEntityId(String serverId, String userId) {
        this.serverId = serverId;
        this.userId = userId;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
