package com.paladin.discord.bot.component;

import com.paladin.discord.bot.component.commands.misc.AfkCommand;
import com.paladin.discord.bot.component.commands.moderation.ServerBlacklistCommand;
import com.paladin.discord.bot.component.commands.owner.VerifyServerCommand;
import com.paladin.discord.bot.config.BotConfig;
import com.paladin.discord.bot.util.LogUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
public class PaladinEventListener extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaladinEventListener.class);
    private int startedShards = 0;
    private final BotConfig botConfig;
    private final CommandManager manager;
    private final AfkCommand afkCommand;
    private final VerifyServerCommand verifyServerCommand;

    public PaladinEventListener(BotConfig botConfig,
                                CommandManager manager,
                                AfkCommand afkCommand,
                                VerifyServerCommand verifyServerCommand) {
        this.botConfig = botConfig;
        this.manager = manager;
        this.afkCommand = afkCommand;
        this.verifyServerCommand = verifyServerCommand;
    }

    // Getting fired for every shard that starts/ is ready
    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        startedShards++;
        LogUtils.whenNoGuilds_thenLogInviteUrl(event.getJDA());
        int memberCount = event.getJDA().getGuilds().stream().mapToInt(Guild::getMemberCount).sum();
        LogUtils.logShardStartup(event.getJDA().getShardInfo().getShardId(), event.getJDA().getGuilds().size(),
                memberCount);
        if (startedShards == botConfig.getShardCount()) {
            LogUtils.logBotStartup(event.getJDA().getSelfUser().getAsTag(),
                    event.getJDA().getSelfUser().getId(),
                    botConfig.getShardCount(),
                    botConfig.getDefaultPrefix());
        }

        if (botConfig.isServerVerificationEnabled()) {
            event.getJDA().getGuilds().forEach(verifyServerCommand::leaveGuildIfNotVerified);
        }
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        // TODO: Add property to enable and disable message logging
        LOGGER.debug("{}/[{}]: {}", event.isFromGuild() ? event.getGuild().getName() : "DM",
                event.getAuthor().getName(),
                event.getMessage().getContentDisplay());
        // bots and webhooks
        if (event.isWebhookMessage() || event.getAuthor().isBot()) {
            return;
        }
        // handle afk state
        afkCommand.handleAfkStateOfUser(event);
        // commands
        manager.handle(event);
    }

    @Override
    public void onSlashCommand(@Nonnull SlashCommandEvent event) {
        LOGGER.debug("Received slash command: {} by user {}", event.getName(), event.getUser().getAsTag());
        // commands
        manager.handle(event);
    }

    @Override
    public void onSelectionMenu(@NotNull SelectionMenuEvent event) {
        manager.handle(event);
    }

    @Override
    // Discord triggers this event on server errors too!
    public void onGuildJoin(@Nonnull GuildJoinEvent event) {
        verifyServerCommand.leaveGuildIfNotVerified(event.getGuild());
    }
}