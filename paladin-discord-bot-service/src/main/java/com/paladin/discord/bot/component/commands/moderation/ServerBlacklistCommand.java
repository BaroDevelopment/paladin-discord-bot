package com.paladin.discord.bot.component.commands.moderation;

import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.paladin.discord.bot.component.CommandManager;
import com.paladin.discord.bot.component.commands.ACommand;
import com.paladin.discord.bot.component.commands.CommandContext;
import com.paladin.discord.bot.component.commands.ICommand;
import com.paladin.discord.bot.component.commands.ISlashCommand;
import com.paladin.discord.bot.config.EmoteConfig;
import com.paladin.discord.bot.entities.blacklistcommand.ServerBlacklistCommandEntity;
import com.paladin.discord.bot.entities.blacklistcommand.ServerBlacklistCommandEntityId;
import com.paladin.discord.bot.enums.CommandCategory;
import com.paladin.discord.bot.repository.database.ServerBlacklistCommandRepository;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
public class ServerBlacklistCommand extends ACommand implements ICommand, ISlashCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerBlacklistCommand.class);
    private static final String COMMAND_NAME = "serverblacklist";
    private final EmoteConfig emoteConfig;
    private final ServerBlacklistCommandRepository repository;
    private static final String OPTION_ADD = "add";
    private static final String OPTION_REMOVE = "remove";
    private static final String OPTION_DELETE = "delete";

    public ServerBlacklistCommand(EmoteConfig emoteConfig, ServerBlacklistCommandRepository repository) {
        this.emoteConfig = emoteConfig;
        this.repository = repository;
    }

    public boolean isBlacklisted(String guildId, String userId) {
        LOGGER.debug("Checking if user with ID {} is blacklisted in server with ID {}", userId, guildId);
        ServerBlacklistCommandEntityId id = new ServerBlacklistCommandEntityId(guildId, userId);
        return repository.findById(id).isPresent();
    }

    public void blacklist(String guildId, String userId) {
        LOGGER.debug("Blacklisted userId {} in guild with ID {}", userId, guildId);
        ServerBlacklistCommandEntityId id = new ServerBlacklistCommandEntityId(guildId, userId);
        repository.save(new ServerBlacklistCommandEntity(id));
    }

    public void unblacklist(String guildId, String userId) {
        LOGGER.debug("Unblacklisted user with id {} in guild with id {}", userId, guildId);
        ServerBlacklistCommandEntityId id = new ServerBlacklistCommandEntityId(guildId, userId);
        repository.delete(new ServerBlacklistCommandEntity(id));
    }

    @Override
    public void execute(CommandContext ctx) {
        String option = ctx.getArgs().split(" ")[0].toLowerCase();
        String userId = ctx.getArgs().split(" ")[1];
        switch (option) {
            case OPTION_ADD:
                blacklist(ctx.getEvent().getGuild().getId(), userId);
                ctx.getEvent().getChannel().sendMessageEmbeds(
                        sendSuccess("Blacklisted user with ID " + userId).build()).queue();
                break;
            case OPTION_REMOVE:
            case OPTION_DELETE:
                unblacklist(ctx.getEvent().getGuild().getId(), userId);
                ctx.getEvent().getChannel().sendMessageEmbeds(
                        sendSuccess("Unblacklisted user with ID " + userId).build()).queue();
                break;
            default:
                LOGGER.debug("Invalid option: {} provided", option);
        }
    }

    @Override
    public void handleSlashCommand(SlashCommandEvent event) {
        boolean blacklist = Boolean.parseBoolean(event.getOptions().get(0).getAsString());
        User user = event.getOptions().get(1).getAsUser();
        if (blacklist) {
            blacklist(Objects.requireNonNull(event.getGuild()).getId(), user.getId());
            event.replyEmbeds(sendSuccess("Blacklisted " + user.getAsMention()).build())
                    .setEphemeral(true)
                    .queue();
        } else {
            unblacklist(Objects.requireNonNull(event.getGuild()).getId(), user.getId());
            event.replyEmbeds(sendSuccess("Unblacklisted " + user.getAsMention()).build())
                    .setEphemeral(true)
                    .queue();
        }
    }

    @Override
    public CommandData getSlashCommand() {
        return new CommandData(getName().toLowerCase(), getDescription())
                .addOption(OptionType.BOOLEAN, "blacklist", "If true then blacklist and unblacklist otherwise", true)
                .addOption(OptionType.USER, "user", "The user to blacklist/unblacklist", true);
    }

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public boolean isGuildSlashCommand() {
        return false;
    }

    @Override
    public boolean validate(SlashCommandEvent event) {
        String userId = event.getOptions().get(1).getAsString();
        return FinderUtil.DISCORD_ID.matcher(userId).matches();
    }

    @Override
    @PostConstruct
    public void init() {
        CommandManager.COMMANDS.put(COMMAND_NAME, this);
        CommandManager.SLASH_COMMANDS.put(COMMAND_NAME, this);
    }

    @Override
    public boolean validate(CommandContext ctx) {
        if (ctx.getArgs().split(" ").length != 2) {
            ctx.getEvent().getChannel().sendMessageEmbeds(getUsage()).queue();
            return false;
        }
        // check if argument
        String option = ctx.getArgs().split(" ")[0];
        String userId = ctx.getArgs().split(" ")[1];
        boolean validOption = option.equals(OPTION_ADD) ||
                option.equals(OPTION_DELETE) ||
                option.equals(OPTION_REMOVE);

        if (!FinderUtil.DISCORD_ID.matcher(userId).matches() || !validOption) {
            ctx.getEvent().getChannel().sendMessageEmbeds(getUsage()).queue();
            return false;
        }

        return true;
    }

    @Override
    public String getDescription() {
        return "Blacklist a user in a server and prevent bot usage";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MODERATION;
    }

    @Override
    public boolean needArgs() {
        return true;
    }

    @Override
    public boolean isGuildOnlyCommand() {
        return true;
    }

    @Override
    public List<MessageEmbed> getUsage() {
        String format = "<" + OPTION_ADD + "/" + OPTION_DELETE + "> ";
        String usage = "```css\n." + COMMAND_NAME + " " + format + "<user_id>\n```";
        return Collections.singletonList(
                getUsageEmbed(this, emoteConfig)
                        .addField("Usage", usage, false).build()
        );
    }

    @Override
    public Set<String> getAliases() {
        return Set.of("sbl");
    }

    @Override
    public long getCooldown() {
        return 0;
    }

    @Override
    public EnumSet<Permission> getMemberPermissions() {
        return EnumSet.of(Permission.MANAGE_ROLES, Permission.ADMINISTRATOR);
    }

    @Override
    public EnumSet<Permission> getBotPermissions() {
        return EnumSet.of(Permission.MANAGE_ROLES, Permission.ADMINISTRATOR);
    }
}
