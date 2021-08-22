package com.paladin.discord.bot.component.commands;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import javax.annotation.PostConstruct;

public interface ISlashCommand {

    @PostConstruct
    void init();

    String getName();

    CommandData getSlashCommand();

    void handleSlashCommand(SlashCommandEvent event);

    default boolean isGuildSlashCommand(){
        return false;
    }

    boolean validate(SlashCommandEvent event);

    // in seconds
    long getCooldown();
}