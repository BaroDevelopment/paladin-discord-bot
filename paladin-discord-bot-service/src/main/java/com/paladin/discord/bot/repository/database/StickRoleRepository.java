package com.paladin.discord.bot.repository.database;

import com.paladin.discord.bot.entities.stickyroles.StickyRoleEntity;
import com.paladin.discord.bot.entities.stickyroles.StickyRoleEntityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StickRoleRepository extends JpaRepository<StickyRoleEntity, StickyRoleEntityId> {

}