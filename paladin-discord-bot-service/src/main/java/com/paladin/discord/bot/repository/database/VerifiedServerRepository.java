package com.paladin.discord.bot.repository.database;

import com.paladin.discord.bot.entities.VerifiedServerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerifiedServerRepository extends JpaRepository<VerifiedServerEntity, String> {

}