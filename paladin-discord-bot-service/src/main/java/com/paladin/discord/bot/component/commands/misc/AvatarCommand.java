package com.paladin.discord.bot.component.commands.misc;

import com.paladin.discord.bot.component.CommandManager;
import com.paladin.discord.bot.component.commands.ACommand;
import com.paladin.discord.bot.component.commands.CommandContext;
import com.paladin.discord.bot.component.commands.ICommand;
import com.paladin.discord.bot.component.commands.ISlashCommand;
import com.paladin.discord.bot.config.EmoteConfig;
import com.paladin.discord.bot.enums.CommandCategory;
import com.paladin.discord.bot.util.ColorUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class AvatarCommand extends ACommand implements ICommand, ISlashCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(AvatarCommand.class);
    private static final  String COMMAND_NAME = "avatar";
    private static EmoteConfig emoteConfig;

    public AvatarCommand(EmoteConfig emoteConfig) {
        AvatarCommand.emoteConfig = emoteConfig;
    }

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public CommandData getSlashCommand() {
        return new CommandData(getName(), getDescription())
                .addOption(OptionType.USER, "member", "The member to get the avatar");
    }

    @Override
    public void handleSlashCommand(SlashCommandEvent event) {
        List<User> mentionedUsers = event.getOptions().stream().map(OptionMapping::getAsUser)
                .collect(Collectors.toList());
        
        if (mentionedUsers.isEmpty()) {
            event.replyEmbeds(getAvatarMessageEmbed(event.getUser())).queue();
        }

        for (User user : mentionedUsers) {
            event.replyEmbeds(getAvatarMessageEmbed(user)).queue();
        }
    }

    private MessageEmbed getAvatarMessageEmbed(User user) {
        EmbedBuilder eb = new EmbedBuilder().setColor(ColorUtil.getRandomHsbColor());
        eb.setAuthor(user.getName(), user.getEffectiveAvatarUrl(), user.getEffectiveAvatarUrl());
        eb.setImage(user.getEffectiveAvatarUrl() + "?size=1024");
        LOGGER.debug("Sending avatar embed of user {}", user.getAsTag());
        return eb.build();
    }

    @Override
    public boolean isGuildSlashCommand() {
        return false;
    }

    @Override
    public boolean validate(SlashCommandEvent event) {
        return true;
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
    public void execute(CommandContext ctx) {
        for (User user : findUser(ctx)) {
            ctx.getEvent().getTextChannel().sendMessageEmbeds(getAvatarMessageEmbed(user)).queue();
        }
    }

    private List<User> findUser(CommandContext ctx) {

        if (ctx.getArgs().isEmpty()) {
            return Collections.singletonList(ctx.getEvent().getAuthor());
        }

        if (!ctx.getEvent().getMessage().getMentionedMembers().isEmpty()) {
            return ctx.getEvent().getMessage().getMentionedMembers()
                    .stream()
                    .map(Member::getUser)
                    .collect(Collectors.toList());
        }
        // TODO: @Stalitsa handle multiple userId's as arguments
        // TODO: @Stalitsa handle multiple usernames as arguments using a delimiter
        return ctx.getEvent().getGuild().getMembers().stream()
                .filter(member -> member.getUser().getId().equals(ctx.getArgs()) || member.getUser().getName().equals(ctx.getArgs()))
                .map(Member::getUser)
                .collect(Collectors.toList());
    }

    @Override
    public String getDescription() {
        return "Returns the avatar of provided user(s)";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MISC;
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
        String usageMention = "```css\n." + COMMAND_NAME + " <@user1, @user2 ...>\n```";
        String usageUsername = "```css\n." + COMMAND_NAME + "<user_name>\n```";
        String usageUserId = "```css\n." + COMMAND_NAME + " <user_id>\n```";
        return Collections.singletonList(
                getUsageEmbed(this, emoteConfig)
                        .addField("Usage - Mention", usageMention, false)
                        .addField("Usage - Username", usageUsername, false)
                        .addField("Usage - UserID", usageUserId, false).build()
        );
    }

    @Override
    public Set<String> getAliases() {
        return Set.of("pfp");
    }

    @Override
    public long getCooldown() {
        return 0;
    }
}
