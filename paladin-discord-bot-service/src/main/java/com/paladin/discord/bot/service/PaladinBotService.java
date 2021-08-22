package com.paladin.discord.bot.service;

import com.paladin.discord.bot.component.PaladinEventListener;
import com.paladin.discord.bot.config.BotConfig;
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;
import java.util.EnumSet;

@Service
public class PaladinBotService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaladinBotService.class);
    private final BotConfig botConfig;
    private final PaladinEventListener eventListener;
    private ShardManager shardManager;

    public PaladinBotService(BotConfig botConfig, PaladinEventListener eventListener) {
        this.botConfig = botConfig;
        this.eventListener = eventListener;
    }

    @Bean
    private void createJda() throws LoginException {
        LOGGER.debug("Creating JDA ...");
        shardManager = DefaultShardManagerBuilder
                .createDefault(botConfig.getToken())
                .setBulkDeleteSplittingEnabled(true)
                .setAudioSendFactory(new NativeAudioSendFactory())
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(EnumSet.allOf(GatewayIntent.class))
                .addEventListeners(eventListener)
                .setShardsTotal(botConfig.getShardCount())
                .build();
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    public PaladinEventListener getEventListener() {
        return eventListener;
    }

    public BotConfig getBotConfig() {
        return botConfig;
    }
}
