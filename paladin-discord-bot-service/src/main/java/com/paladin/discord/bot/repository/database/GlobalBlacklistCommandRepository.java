package com.paladin.discord.bot.repository.database;

import com.paladin.discord.bot.entities.blacklistcommand.GlobalBlacklistCommandEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GlobalBlacklistCommandRepository extends JpaRepository<GlobalBlacklistCommandEntity, String> {

}
