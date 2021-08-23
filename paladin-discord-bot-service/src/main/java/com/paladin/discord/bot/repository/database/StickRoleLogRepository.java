package com.paladin.discord.bot.repository.database;

import com.paladin.discord.bot.entities.stickyroles.StickyRoleLogEntity;
import com.paladin.discord.bot.entities.stickyroles.StickyRoleLogEntityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StickRoleLogRepository extends JpaRepository<StickyRoleLogEntity, StickyRoleLogEntityId> {

    @Query("SELECT stickyRoleLogEntityId FROM StickyRoleLogEntity WHERE stickyRoleLogEntityId.serverId = :serverId " +
            "and stickyRoleLogEntityId.userId = :userId")
    List<StickyRoleLogEntityId> findRolesOfLeftUser(String serverId, String userId);
}