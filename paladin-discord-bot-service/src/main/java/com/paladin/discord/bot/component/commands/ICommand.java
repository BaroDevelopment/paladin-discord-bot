package com.paladin.discord.bot.component.commands;

import com.paladin.discord.bot.enums.CommandCategory;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public interface ICommand {

    String getName();

    @PostConstruct
    void init();

    boolean validate(CommandContext ctx);

    void execute(CommandContext ctx);

    String getDescription();

    CommandCategory getCategory();

    boolean needArgs();

    boolean isGuildOnlyCommand();

    default boolean isNsfw() {
        return false;
    }

    default boolean isAnnouncementOnly() {
        return false;
    }

    List<MessageEmbed> getUsage();

    default Set<String> getAliases() {
        return Collections.emptySet();
    }

    default EnumSet<Permission> getMemberPermissions() {
        return EnumSet.noneOf(Permission.class);
    }

    default EnumSet<Permission> getBotPermissions() {
        return EnumSet.noneOf(Permission.class);
    }

    // in seconds
    long getCooldown();
}