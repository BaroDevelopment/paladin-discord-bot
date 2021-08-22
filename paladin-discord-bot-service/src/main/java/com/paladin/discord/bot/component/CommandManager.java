package com.paladin.discord.bot.component;

import com.paladin.discord.bot.component.commands.CommandContext;
import com.paladin.discord.bot.component.commands.ICommand;
import com.paladin.discord.bot.component.commands.ISelectionMenuCommand;
import com.paladin.discord.bot.component.commands.ISlashCommand;
import com.paladin.discord.bot.component.commands.moderation.ServerBlacklistCommand;
import com.paladin.discord.bot.config.BotConfig;
import com.paladin.discord.bot.enums.CommandCategory;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class CommandManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandManager.class);
    public static final Map<String, ICommand> COMMANDS = new HashMap<>();
    public static final Map<String, ISlashCommand> SLASH_COMMANDS = new HashMap<>();
    public static final Map<String, ISelectionMenuCommand> SELECTION_MENU_COMMANDS = new HashMap<>();
    private final BotConfig botConfig;
    private final ServerBlacklistCommand serverBlacklistCommand;


    public CommandManager(BotConfig botConfig, ServerBlacklistCommand serverBlacklistCommand) {
        this.botConfig = botConfig;
        this.serverBlacklistCommand = serverBlacklistCommand;
    }

    public void handle(MessageReceivedEvent event) {

        String prefix = event.getChannelType() == ChannelType.PRIVATE ? botConfig.getDefaultPrefix() :
                getPrefix(event.getGuild().getIdLong());

        if (!event.getMessage().getContentRaw().startsWith(prefix)) return;

        // remove prefix but keep invoke
        String[] args = event.getMessage().getContentRaw()
                .replaceFirst("(?i)" + Pattern.quote(prefix), "")
                .split("\\s+");
        String invoke = args[0].toLowerCase();
        ICommand cmd = getBaseCommand(invoke);

        if (cmd == null) return;

        CommandContext ctx = new CommandContext(prefix, invoke, args, event);

        if (!argsProvided(ctx, cmd)) {
            event.getChannel().sendMessageEmbeds(cmd.getUsage()).queue();
            return;
        }
        if (isGuildOnlyViolation(ctx, cmd)) {
            event.getChannel().sendMessage("This command is not available in private channels").queue();
            return;
        }
        if (isNsfwViolation(ctx, cmd)) {
            event.getChannel().sendMessage("This is a nsfw command. Please execute it in a nsfw channel.").queue();
            return;
        }
        if (!hasAllMemberPermissions(ctx, cmd)) {
            event.getChannel().sendMessage("You don't have needed permissions to run this command!" +
                    "\nPermissions needed:\n" + cmd.getMemberPermissions().toString()).queue();
            return;
        }
        if (!hasAllBotPermissions(ctx, cmd)) {
            event.getChannel().sendMessage("I don't have needed permissions to run this command!" +
                    "\nI need the following permissions:\n" + cmd.getBotPermissions().toString()).queue();
            return;
        }

        if (!hasOwnerPermissions(ctx, cmd)) {
            LOGGER.debug("User {} tried to execute owner command {} {}", event.getAuthor().getAsTag(), invoke, ctx.getArgs());
            return;
        }

        if (!cmd.validate(ctx)) {
            LOGGER.debug("Validation failed");
            return;
        }

        // handle server blacklist
        if (serverBlacklistCommand.isBlacklisted(event.getGuild().getId(), event.getAuthor().getId())) {
            LOGGER.debug("Ignoring blacklisted user with ID {} in guild with ID {}",
                    event.getGuild().getId(), event.getAuthor().getId());
            return;
        }

        cmd.execute(ctx);
    }

    public void handle(SlashCommandEvent event) {

        ISlashCommand cmd = getSlashCommand(event.getName());

        if (cmd == null) return;

        if (!cmd.validate(event)) return;

        // handle server blacklist
        if (event.getGuild() != null && serverBlacklistCommand.isBlacklisted(event.getGuild().getId(), event.getUser().getId())) {
            LOGGER.debug("Ignoring blacklisted user with ID {} in guild with ID {}",
                    event.getGuild().getId(), event.getUser().getId());
            return;
        }

        cmd.handleSlashCommand(event);
    }

    public void handle(SelectionMenuEvent event) {
        ISelectionMenuCommand cmd = getSelectionMenuCommand(event.getComponentId());

        if (cmd == null) return;

        cmd.handleSelectionMenu(event);
    }

    private boolean hasOwnerPermissions(CommandContext ctx, ICommand cmd) {
        if (cmd.getCategory().equals(CommandCategory.OWNER) &&
                !botConfig.getBotOwnerIds().contains(ctx.getEvent().getAuthor().getId())) {
            ctx.getEvent().getChannel().sendMessage("Only a bot owner can execute this command!").queue();
            return false;
        }
        return true;
    }

    private boolean hasAllMemberPermissions(CommandContext ctx, ICommand cmd) {

        if (!ctx.getEvent().getChannel().getType().equals(ChannelType.TEXT)) return true;
        if (ctx.getEvent().getMember() == null) return false;

        EnumSet<Permission> neededPerms = cmd.getMemberPermissions();
        EnumSet<Permission> memberPermissions = ctx.getEvent().getMember().getPermissions();

        if (neededPerms == null) return true;

        return memberPermissions.contains(Permission.ADMINISTRATOR) ||
                memberPermissions.containsAll(neededPerms) ||
                botConfig.getBotOwnerIds().contains(ctx.getEvent().getAuthor().getId());
    }

    private boolean hasAllBotPermissions(CommandContext ctx, ICommand cmd) {
        if (!ctx.getEvent().getChannel().getType().equals(ChannelType.TEXT)) return true;
        Member member = ctx.getEvent().getGuild().getMemberById(ctx.getEvent().getJDA().getSelfUser().getId());
        if (member == null) return false;
        EnumSet<Permission> neededPermissions = cmd.getBotPermissions();
        EnumSet<Permission> botPermissions = member.getPermissions();

        if (neededPermissions == null) return true;

        return botPermissions.containsAll(neededPermissions);
    }

    private boolean argsProvided(CommandContext ctx, ICommand cmd) {
        return !ctx.getArgs().isEmpty() || !cmd.needArgs();
    }

    private boolean isGuildOnlyViolation(CommandContext ctx, ICommand cmd) {
        return !ctx.getEvent().getChannel().getType().equals(ChannelType.TEXT) && cmd.isGuildOnlyCommand();
    }

    private boolean isNsfwViolation(CommandContext ctx, ICommand cmd) {
        boolean notTextChannel = !ctx.getEvent().getChannel().getType().equals(ChannelType.TEXT);
        if (notTextChannel) return false;
        return !ctx.getEvent().getTextChannel().isNSFW() && cmd.isNsfw();
    }

    public @NotNull String getPrefix(long serverId) {
        return botConfig.getDefaultPrefix();
    }

    public @Nullable ICommand getBaseCommand(String search) {
        String searchLower = search.toLowerCase();
        for (ICommand cmd : COMMANDS.values()) {
            if (cmd.getName().equalsIgnoreCase(searchLower) || cmd.getAliases().contains(searchLower)) {
                return cmd;
            }
        }

        return null;
    }

    public @Nullable ISlashCommand getSlashCommand(String search) {
        String searchLower = search.toLowerCase();
        for (ISlashCommand cmd : SLASH_COMMANDS.values()) {
            if (cmd.getName().equalsIgnoreCase(searchLower)) {
                return cmd;
            }
        }

        return null;
    }

    public @Nullable ISelectionMenuCommand getSelectionMenuCommand(String search) {
        String searchLower = search.toLowerCase();
        for (ISelectionMenuCommand cmd : SELECTION_MENU_COMMANDS.values()) {
            if (cmd.getName().equalsIgnoreCase(searchLower)) {
                return cmd;
            }
        }

        return null;
    }

    public Map<String, ICommand> getCommands() {
        return COMMANDS;
    }

    public BotConfig getBotConfig() {
        return botConfig;
    }

}
