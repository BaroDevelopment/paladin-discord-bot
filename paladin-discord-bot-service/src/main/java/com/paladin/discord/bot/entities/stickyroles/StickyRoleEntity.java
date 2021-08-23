package com.paladin.discord.bot.entities.stickyroles;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity(name = "StickyRoleEntity")
@Table(name = "Sticky_Role")
public class StickyRoleEntity {

    @EmbeddedId
    private StickyRoleEntityId stickyRoleEntityId;

    public StickyRoleEntity() {
    }

    public StickyRoleEntity(StickyRoleEntityId stickyRoleEntityId) {
        this.stickyRoleEntityId = stickyRoleEntityId;
    }

    public StickyRoleEntityId getStickyRolesEntityId() {
        return stickyRoleEntityId;
    }

    public void setStickyRolesEntityId(StickyRoleEntityId stickyRoleEntityId) {
        this.stickyRoleEntityId = stickyRoleEntityId;
    }
}