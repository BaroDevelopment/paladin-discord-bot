package com.paladin.discord.bot.component.commands.information;

import com.paladin.discord.bot.component.CommandManager;
import com.paladin.discord.bot.component.commands.ACommand;
import com.paladin.discord.bot.component.commands.CommandContext;
import com.paladin.discord.bot.component.commands.ICommand;
import com.paladin.discord.bot.component.commands.ISlashCommand;
import com.paladin.discord.bot.config.EmoteConfig;
import com.paladin.discord.bot.enums.CommandCategory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class InviteCommand extends ACommand implements ICommand, ISlashCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(InviteCommand.class);
    private static final String COMMAND_NAME = "invite";
    private static final String BOT_SCOPE = "scope=bot%20applications.commands";
    private final EmoteConfig emoteConfig;
    private static final String JOIN_MESSAGE = "Click here to join the bot to your server";
    private static final String JOIN_FORMAT = "[%S](%S)";

    public InviteCommand(EmoteConfig emoteConfig) {
        this.emoteConfig = emoteConfig;
    }

    private String getInviteUrl(JDA jda) {
        return jda.getInviteUrl(Permission.ADMINISTRATOR).replace("scope=bot", BOT_SCOPE);
    }

    @Override
    public void execute(CommandContext ctx) {
        String inviteUrl = getInviteUrl(ctx.getEvent().getJDA());
        LOGGER.debug("Sending invite url: {}", inviteUrl);
        EmbedBuilder eb = sendSuccess(String.format(JOIN_FORMAT, JOIN_MESSAGE, inviteUrl));
        ctx.getEvent().getChannel().sendMessageEmbeds(eb.build()).queue();
    }

    @Override
    public void handleSlashCommand(SlashCommandEvent event) {
        LOGGER.debug("Sending invite url: {}", getInviteUrl(event.getJDA()));
        event.reply("Click the button to join the bot to your server")
                .addActionRow(Button.link(getInviteUrl(event.getJDA()), "Invite")
                        .withEmoji(Emoji.fromMarkdown(emoteConfig.getPaladin())))
                .queue();
    }

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public String getDescription() {
        return "Get bot's invite url";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.INFORMATION;
    }

    @Override
    public boolean needArgs() {
        return false;
    }

    @Override
    public boolean isGuildOnlyCommand() {
        return false;
    }

    @Override
    public List<MessageEmbed> getUsage() {
        return Collections.singletonList(getUsageEmbed(this, emoteConfig).build());
    }

    @Override
    public Set<String> getAliases() {
        return Set.of("inv");
    }

    @Override
    public long getCooldown() {
        return 60;
    }

    @Override
    @PostConstruct
    public void init() {
        CommandManager.COMMANDS.put(COMMAND_NAME, this);
        CommandManager.SLASH_COMMANDS.put(COMMAND_NAME, this);
    }

    @Override
    public boolean validate(CommandContext ctx) {
        return true;
    }

    @Override
    public boolean validate(SlashCommandEvent event) {
        return true;
    }


    @Override
    public CommandData getSlashCommand() {
        return new CommandData(getName(), getDescription());
    }
}