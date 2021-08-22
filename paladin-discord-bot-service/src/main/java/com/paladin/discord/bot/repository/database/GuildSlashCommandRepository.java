package com.paladin.discord.bot.repository.database;

import com.paladin.discord.bot.entities.guildslashcommand.GuildSlashCommandEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuildSlashCommandRepository extends JpaRepository<GuildSlashCommandEntity, Long> {

}
