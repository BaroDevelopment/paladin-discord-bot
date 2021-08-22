package com.paladin.discord.bot.repository.database;

import com.paladin.discord.bot.entities.blacklistcommand.ServerBlacklistCommandEntity;
import com.paladin.discord.bot.entities.blacklistcommand.ServerBlacklistCommandEntityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerBlacklistCommandRepository extends JpaRepository<ServerBlacklistCommandEntity, ServerBlacklistCommandEntityId> {

}
