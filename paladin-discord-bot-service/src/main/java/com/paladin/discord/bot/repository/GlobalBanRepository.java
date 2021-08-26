package com.paladin.discord.bot.repository;

import com.paladin.discord.bot.entities.GlobalBanEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GlobalBanRepository extends CrudRepository<GlobalBanEntity, String> {
}