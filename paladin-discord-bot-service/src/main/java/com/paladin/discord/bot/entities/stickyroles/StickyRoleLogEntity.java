package com.paladin.discord.bot.entities.stickyroles;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity(name = "StickyRoleLogEntity")
@Table(name = "Sticky_Role_Log")
public class StickyRoleLogEntity {

    @EmbeddedId
    private StickyRoleLogEntityId stickyRoleLogEntityId;

    public StickyRoleLogEntity() {
    }

    public StickyRoleLogEntity(StickyRoleLogEntityId stickyRoleLogEntityId) {
        this.stickyRoleLogEntityId = stickyRoleLogEntityId;
    }

    public StickyRoleLogEntityId getStickyRoleLogEntityId() {
        return stickyRoleLogEntityId;
    }

    public void setStickyRoleLogEntityId(StickyRoleLogEntityId stickyRoleLogEntityId) {
        this.stickyRoleLogEntityId = stickyRoleLogEntityId;
    }
}