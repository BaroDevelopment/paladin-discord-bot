package com.paladin.discord.bot.util;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LogUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogUtils.class);

    public static void logBotStartup(String tag, String userId, int totalShardCount, String defaultPrefix) {
        LOGGER.info("{} --------------------------------- {}", ConsoleColors.BLUE_BOLD_BRIGHT, ConsoleColors.RESET);
        LOGGER.info("{} Logged in as: {} {}", ConsoleColors.PURPLE_BOLD_BRIGHT, tag, ConsoleColors.RESET);
        LOGGER.info("{} UserID: {} {}", ConsoleColors.PURPLE_BOLD_BRIGHT, userId, ConsoleColors.RESET);
        LOGGER.info("{} Total Shard Count: {} {}", ConsoleColors.PURPLE_BOLD_BRIGHT, totalShardCount,
                ConsoleColors.RESET);
        LOGGER.info("{} Default Prefix: {} {}", ConsoleColors.PURPLE_BOLD_BRIGHT, defaultPrefix, ConsoleColors.RESET);
        LOGGER.info("{} ---------------------------------{}", ConsoleColors.BLUE_BOLD_BRIGHT, ConsoleColors.RESET);
    }

    public static void logShardStartup(int shardId, int guildCount, int memberCount) {
        LOGGER.info("{}Started Shard {} with {} guilds and {} members{}",
                ConsoleColors.CYAN_BOLD_BRIGHT,
                shardId,
                guildCount,
                memberCount,
                ConsoleColors.RESET);
    }

    public static void whenNoGuilds_thenLogInviteUrl(JDA jda) {
        if (jda.getGuilds().isEmpty()) {
            LOGGER.warn("This bot is not on any guilds! Use the following link to add the bot to your guilds!");
            LOGGER.warn(jda.getInviteUrl(Permission.ADMINISTRATOR));
        }
    }
}