package com.paladin.discord.bot.component.commands.owner;

import com.paladin.discord.bot.component.CommandManager;
import com.paladin.discord.bot.component.commands.*;
import com.paladin.discord.bot.config.EmoteConfig;
import com.paladin.discord.bot.entities.guildslashcommand.GuildSlashCommandEntity;
import com.paladin.discord.bot.enums.CommandCategory;
import com.paladin.discord.bot.repository.database.GuildSlashCommandRepository;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class UpdateSlashCommands extends ACommand implements ICommand, ISlashCommand, ISelectionMenuCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateSlashCommands.class);
    private static final String COMMAND_NAME = "updateSlash";
    private static final String GUILD_ARGUMENT = "guild";
    private static final String GLOBAL_ARGUMENT = "global";
    private static final String ADD_ARGUMENT = "add";
    private static final String ADD_GUILD_SLASH_COMMANDS = "add_guild_commands";
    private static final String ADD_GLOBAL_SLASH_COMMANDS = "add_global_commands";
    private static final String REMOVE_GLOBAL_SLASH_COMMANDS = "remove_global_commands";
    private static final String REMOVE_GUILD_SLASH_COMMANDS = "remove_guild_commands";
    private static final String DELETE_ARGUMENT = "delete";
    private static final String REMOVE_ARGUMENT = "remove";
    private static final String SUCCESS_MSG = "Successfully executed: ";
    private static final String DURATION_MSG = "\nPlease allow up to 1h.";
    private final EmoteConfig emoteConfig;
    private final GuildSlashCommandRepository guildSlashCommandRepository;

    public UpdateSlashCommands(EmoteConfig emoteConfig,
                               GuildSlashCommandRepository guildSlashCommandRepository) {
        this.emoteConfig = emoteConfig;
        this.guildSlashCommandRepository = guildSlashCommandRepository;
    }

    private MessageEmbed deleteAllGuildCommands(Guild guild) {
        if (guild == null) {
            return sendError("Shardmanager is null. Tried to execute " +
                    REMOVE_GUILD_SLASH_COMMANDS).build();
        }

        LOGGER.debug("Deleting all guild slash commands from guild: {}", guild.getName());
        guild.updateCommands().queue(commands -> commands.forEach(command -> command.delete().queue(
                success ->
                        LOGGER.debug("Successfully deleted guild slash command: {}", command.getName()),
                throwable ->
                        LOGGER.debug("Failed to delete guild slash command: {}", command.getName())
        )));
        return sendSuccess(SUCCESS_MSG + REMOVE_GUILD_SLASH_COMMANDS).build();
    }

    private MessageEmbed deleteAllGlobalCommands(JDA jda) {
        LOGGER.debug("Deleting all global slash commands");
        if (jda.getShardManager() == null) {
            return sendError("Shardmanager is null. Tried to execute " +
                    REMOVE_GLOBAL_SLASH_COMMANDS).build();
        }
        jda.getShardManager().getShards()
                .forEach(api -> jda.updateCommands().queue(commands ->
                        commands.forEach(command -> command.delete().queue(
                                success ->
                                        LOGGER.debug("Successfully deleted global slash command: {}",
                                                command.getName()),
                                throwable ->
                                        LOGGER.debug("Failed to delete global slash command: {}", command.getName())
                        ))
                ));
        return sendSuccess(SUCCESS_MSG + REMOVE_GLOBAL_SLASH_COMMANDS + DURATION_MSG).build();
    }

    private MessageEmbed addGuildSlashCommands(JDA jda) {
        try {
            LOGGER.debug("Adding all guild slash command entries from database");

            if (jda.getShardManager() == null) {
                return sendError("Shardmanager is null. Tried to execute " +
                        ADD_GUILD_SLASH_COMMANDS).build();
            }

            List<GuildSlashCommandEntity> records = guildSlashCommandRepository.findAll();
            LOGGER.debug("Found {} entries in database", records.size());
            Set<String> guildIds = records.stream()
                    .map(entry -> entry.getGuildSlashCommandEntityId().getGuildId())
                    .collect(Collectors.toSet());
            LOGGER.debug("{} distinct guilds have custom slash commands", guildIds.size());
            List<Guild> guilds = new ArrayList<>();
            for (String guildId : guildIds) {
                Guild guild = getGuildById(jda.getShardManager(), guildId);
                if (guild != null) {
                    guilds.add(guild);
                }
            }

            for (Guild guild : guilds) {
                List<GuildSlashCommandEntity> entities = records.stream()
                        .filter(e -> e.getGuildSlashCommandEntityId().getGuildId().equals(guild.getId()))
                        .filter(e -> Objects.requireNonNull(CommandManager.SLASH_COMMANDS.get(
                                e.getGuildSlashCommandEntityId().getCommandName())).isGuildSlashCommand())
                        .collect(Collectors.toList());

                List<CommandData> commandDataList = entities.stream()
                        .map(e -> Objects.requireNonNull(
                                CommandManager.SLASH_COMMANDS.get(e.getGuildSlashCommandEntityId().getCommandName())).getSlashCommand())
                        .collect(Collectors.toList());

                CommandListUpdateAction action = guild.updateCommands();
                action.addCommands(commandDataList).queue(
                        commands -> LOGGER.debug("Successfully added {} commands to guild: {}",
                                commands.size(), guild.getName()),
                        throwable ->
                                LOGGER.debug("Failed to add slash command to guild: {}", guild.getName()));
            }
            return sendSuccess(SUCCESS_MSG + ADD_GUILD_SLASH_COMMANDS).build();
        } catch (Exception e) {
            LOGGER.error("Failed to execute {}", ADD_GUILD_SLASH_COMMANDS, e);
            return sendError("Failed to execute: " + ADD_GUILD_SLASH_COMMANDS).build();
        }
    }

    private MessageEmbed addGlobalSlashCommands(JDA jda) {
        if (jda.getShardManager() == null) {
            return sendError("Shardmanager is null. Tried to execute " +
                    ADD_GLOBAL_SLASH_COMMANDS).build();
        }

        for (JDA shard : jda.getShardManager().getShards()) {
            CommandListUpdateAction commandListUpdateAction = shard.updateCommands();
            for (Map.Entry<String, ISlashCommand> command : CommandManager.SLASH_COMMANDS.entrySet()) {
                if (!command.getValue().isGuildSlashCommand() && command.getValue().getSlashCommand() != null) {
                    commandListUpdateAction.addCommands(command.getValue().getSlashCommand()).queue(
                            commands ->
                                    LOGGER.debug("Slash commands updated for Shard {}. There are {} commands present",
                                            jda.getShardInfo().getShardId(), commands.size()),
                            throwable ->
                                    LOGGER.error("Failed to update slash commands for Shard {}.",
                                            jda.getShardInfo().getShardId()));
                }
            }
        }
        return sendSuccess(SUCCESS_MSG + ADD_GLOBAL_SLASH_COMMANDS + DURATION_MSG).build();
    }

    @Override
    public void execute(CommandContext ctx) {

        boolean isGuild = ctx.getArgs().split(" ")[0].equals(GUILD_ARGUMENT);
        boolean isAddd = ctx.getArgs().split(" ")[1].equals(ADD_ARGUMENT);

        if (isGuild && isAddd) {
            ctx.getEvent().getChannel().sendMessageEmbeds(addGuildSlashCommands(ctx.getEvent().getJDA())).queue();
        } else if (isGuild) {
            ctx.getEvent().getChannel().sendMessageEmbeds(deleteAllGuildCommands(ctx.getEvent().getGuild())).queue();
        } else if (isAddd) { // global add
            ctx.getEvent().getChannel().sendMessageEmbeds(addGlobalSlashCommands(ctx.getEvent().getJDA())).queue();
        } else { // global delete
            ctx.getEvent().getChannel().sendMessageEmbeds(deleteAllGlobalCommands(ctx.getEvent().getJDA())).queue();
        }
    }

    @Override
    public boolean validate(CommandContext ctx) {
        // check args length
        if (ctx.getArgs().split(" ").length != 2) {
            ctx.getEvent().getChannel().sendMessageEmbeds(getUsage()).queue();
            return false;
        }

        boolean isGuild = ctx.getArgs().split(" ")[0].equals(GUILD_ARGUMENT);
        boolean isGlobal = ctx.getArgs().split(" ")[0].equals(GLOBAL_ARGUMENT);
        boolean isAddd = ctx.getArgs().split(" ")[1].equals(ADD_ARGUMENT);
        boolean isDelete = ctx.getArgs().split(" ")[1].equals(DELETE_ARGUMENT);
        boolean isRemove = ctx.getArgs().split(" ")[1].equals(REMOVE_ARGUMENT);
        boolean rightArguments = (isGuild || isGlobal) && (isAddd || isDelete || isRemove);

        // first argument must be guild or global and second must be add or remove
        if (!rightArguments) {
            ctx.getEvent().getChannel().sendMessageEmbeds(getUsage()).queue();
            return false;
        }

        return true;
    }

    @Override
    public boolean validate(SlashCommandEvent event) {
        return true;
    }

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public CommandData getSlashCommand() {
        return new CommandData(getName().toLowerCase(), getDescription());
    }

    @Override
    public void handleSlashCommand(SlashCommandEvent event) {
        event.reply("Pick your selection")
                .setEphemeral(true)
                .addActionRow(getSelectionMenu())
                .queue();
    }

    @Override
    public boolean isGuildSlashCommand() {
        return true;
    }

    @Override
    public SelectionMenu getSelectionMenu() {
        return SelectionMenu.create(COMMAND_NAME)
                .setPlaceholder("Choose your class") // shows the placeholder indicating what this menu is for
                .setRequiredRange(1, 1) // only one can be selected
                .addOption("Add guild slash cmds", ADD_GUILD_SLASH_COMMANDS)
                .addOption("Add global slash cmds", ADD_GLOBAL_SLASH_COMMANDS)
                .addOption("Delete guild slash cmds", REMOVE_GUILD_SLASH_COMMANDS)
                .addOption("Delete global slash cmds", REMOVE_GLOBAL_SLASH_COMMANDS)
                .build();
    }

    @Override
    public void handleSelectionMenu(SelectionMenuEvent event) {
        List<String> selections = event.getInteraction().getValues();
        if (selections.isEmpty()) {
            LOGGER.error("No selection picked. This should actually never happen.");
        }

        if (selections.size() > 1) {
            LOGGER.error("There was more than one selection. this should actually never happen.");
        }

        String selection = selections.get(0);
        switch (selection) {
            case ADD_GUILD_SLASH_COMMANDS:
                event.replyEmbeds(addGuildSlashCommands(event.getJDA()))
                        .setEphemeral(true)
                        .queue();
                break;
            case ADD_GLOBAL_SLASH_COMMANDS:
                event.replyEmbeds(addGlobalSlashCommands(event.getJDA()))
                        .setEphemeral(true)
                        .queue();
                break;
            case REMOVE_GUILD_SLASH_COMMANDS:
                event.replyEmbeds(deleteAllGuildCommands(event.getGuild()))
                        .setEphemeral(true)
                        .queue();
                break;
            case REMOVE_GLOBAL_SLASH_COMMANDS:
                event.replyEmbeds(deleteAllGlobalCommands(event.getJDA()))
                        .setEphemeral(true)
                        .queue();
                break;
            default:
                LOGGER.error("Selection does not match. This should actually never happen.");
        }
    }

    @Override
    @PostConstruct
    public void init() {
        CommandManager.COMMANDS.put(COMMAND_NAME, this);
        CommandManager.SLASH_COMMANDS.put(COMMAND_NAME, this);
        CommandManager.SELECTION_MENU_COMMANDS.put(COMMAND_NAME, this);
    }

    @Override
    public String getDescription() {
        return "Updates (add or delete) all the global and guild slash commands";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.OWNER;
    }

    @Override
    public boolean needArgs() {
        return true;
    }

    @Override
    public boolean isGuildOnlyCommand() {
        return false;
    }

    @Override
    public List<MessageEmbed> getUsage() {
        String usage = "```css\n." + COMMAND_NAME + " <guild/global> <add/remove>\n```";
        return Collections.singletonList(
                getUsageEmbed(this, emoteConfig)
                        .addField("Usage", usage, false).build()
        );
    }

    @Override
    public long getCooldown() {
        return 0;
    }

    @Override
    public Set<String> getAliases() {
        return Collections.singleton("usc");
    }
}