package com.paladin.discord.bot.entities.stickyroles;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class StickyRoleEntityId implements Serializable {

    private String serverId;
    private String roleId;

    public StickyRoleEntityId() {
    }

    public StickyRoleEntityId(String serverId, String roleId) {
        this.serverId = serverId;
        this.roleId = roleId;
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
}
