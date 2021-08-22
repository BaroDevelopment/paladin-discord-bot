package com.paladin.discord.bot.component.commands;

import com.paladin.discord.bot.config.EmoteConfig;
import com.paladin.discord.bot.util.ColorUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public abstract class ACommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(ACommand.class);

    public EmbedBuilder sendSuccess(String text) {
        EmbedBuilder eb = new EmbedBuilder().setColor(Color.GREEN);
        eb.setDescription(text);
        return eb;
    }

    public EmbedBuilder sendError(String text) {
        EmbedBuilder eb = new EmbedBuilder().setColor(Color.RED);
        eb.setDescription(text);
        return eb;
    }

    public EmbedBuilder sendWarning(String text) {
        EmbedBuilder eb = new EmbedBuilder().setColor(Color.ORANGE);
        eb.setDescription(text);
        return eb;
    }

    public CompletableFuture<Message> sendWaitMessage(CommandContext ctx, String message) {
        return ctx.getEvent().getChannel().sendMessageEmbeds(new EmbedBuilder()
                .setColor(ColorUtil.getRandomHsbColor())
                .setDescription("\u231B " + message)
                .build()).submit();
    }

    public void replayDM(CommandContext ctx, EmbedBuilder eb) {
        ctx.getEvent().getAuthor().openPrivateChannel().queue(
                privateChannel -> privateChannel.sendMessageEmbeds(eb.build()).queue(),
                throwable -> ctx.getEvent().getChannel().sendMessageEmbeds(eb.build()).queue()
        );
    }

    public void replayDM(CommandContext ctx, String message) {
        ctx.getEvent().getAuthor().openPrivateChannel().queue(
                privateChannel -> privateChannel.sendMessage(message).queue(),
                throwable -> ctx.getEvent().getChannel().sendMessage(message).queue()
        );
    }

    public EmbedBuilder getUsageEmbed(ICommand command, EmoteConfig emoteConfig) {
        String memberPermissions = command.getMemberPermissions()
                .stream()
                .map(Permission::getName)
                .collect(Collectors.joining("\n"));
        String botPermissions = command.getBotPermissions()
                .stream()
                .map(Permission::getName)
                .collect(Collectors.joining("\n"));
        String aliases = String.join("\n", command.getAliases());

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle(command.getName())
                .setDescription(command.getDescription())
                .addField("Category", command.getCategory().name(), true)
                .addField("DM", emoteConfig.getBooleanEmote(!command.isGuildOnlyCommand()), true)
                .addField("NSFW", emoteConfig.getBooleanEmote(command.isNsfw()), true)
                .addField("Slash Command", emoteConfig.getBooleanEmote(isSlashCommand(command)), true)
                .addField("Need arguments", emoteConfig.getBooleanEmote(command.needArgs()), true)
                .addField("Cooldown", command.getCooldown() + " seconds", true)
                .setColor(ColorUtil.getRandomHsbColor());

        if (!aliases.isEmpty()) {
            eb.addField("Aliases", aliases, true);
        }

        if (!memberPermissions.isEmpty()) {
            eb.addField("Member Permissions needed", memberPermissions, true);
        }

        if (!botPermissions.isEmpty()) {
            eb.addField("Bot Permissions needed", botPermissions, true);
        }

        return eb;
    }

    public List<Guild> getGuilds(ShardManager shardManager) {
        return shardManager.getShards().stream().map(JDA::getGuilds).flatMap(List::stream).collect(Collectors.toList());
    }

    public @Nullable Guild getGuildById(ShardManager shardManager, String guildId) {
        return shardManager.getShards().stream()
                .map(JDA::getGuilds)
                .flatMap(List::stream)
                .filter(guild -> guild.getId().equals(guildId))
                .findFirst()
                .orElse(null);
    }



    public boolean isSlashCommand(ICommand command) {
        return ISlashCommand.class.isAssignableFrom(command.getClass());
    }

    public boolean isSelectionMenuCommand(ICommand command) {
        return ISelectionMenuCommand.class.isAssignableFrom(command.getClass());
    }
}
