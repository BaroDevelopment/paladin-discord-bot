package com.paladin.discord.bot.entities.stickyroles;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class StickyRoleLogEntityId implements Serializable {

    private String serverId;
    private String roleId;
    private String userId;

    public StickyRoleLogEntityId() {
    }

    public StickyRoleLogEntityId(String serverId, String roleId, String userId) {
        this.serverId = serverId;
        this.roleId = roleId;
        this.userId = userId;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
