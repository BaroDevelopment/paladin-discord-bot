package com.paladin.discord.bot.component.commands;

import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import javax.annotation.PostConstruct;

public interface ISelectionMenuCommand {

    @PostConstruct
    void init();

    String getName();

    SelectionMenu getSelectionMenu();

    void handleSelectionMenu(SelectionMenuEvent event);
}