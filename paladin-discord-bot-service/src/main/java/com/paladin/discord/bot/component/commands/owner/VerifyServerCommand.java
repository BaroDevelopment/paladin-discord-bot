package com.paladin.discord.bot.component.commands.owner;

import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.paladin.discord.bot.component.CommandManager;
import com.paladin.discord.bot.component.commands.ACommand;
import com.paladin.discord.bot.component.commands.CommandContext;
import com.paladin.discord.bot.component.commands.ICommand;
import com.paladin.discord.bot.component.commands.ISlashCommand;
import com.paladin.discord.bot.config.BotConfig;
import com.paladin.discord.bot.config.EmoteConfig;
import com.paladin.discord.bot.entities.VerifiedServerEntity;
import com.paladin.discord.bot.enums.CommandCategory;
import com.paladin.discord.bot.repository.database.VerifiedServerRepository;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class VerifyServerCommand extends ACommand implements ICommand, ISlashCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerifyServerCommand.class);
    private static final String COMMAND_NAME = "verifyServer";
    private final BotConfig botConfig;
    private final EmoteConfig emoteConfig;
    private final VerifiedServerRepository repository;
    private static final String OPTION_GUILD_ID = "server_id";
    private static final String OPTION_VERIFY = "verify";

    public VerifyServerCommand(BotConfig botConfig, EmoteConfig emoteConfig, VerifiedServerRepository repository) {
        this.botConfig = botConfig;
        this.emoteConfig = emoteConfig;
        this.repository = repository;
    }

    public void leaveGuildIfNotVerified(Guild guild) {
        Optional<VerifiedServerEntity> res = repository.findById(guild.getId());
        if (res.isEmpty() && botConfig.isServerVerificationEnabled()) {
            LOGGER.info("Guild {} with ID {} is not verified. Leaving it now.", guild.getName(), guild.getId());
            guild.leave().queue();
            return;
        }

        LOGGER.debug("Server {} with ID {} is verified", guild.getName(), guild.getId());
    }

    public void verifyServer(String guildId) {
        LOGGER.debug("Verifying Guild with id {}", guildId);
        boolean isValidId = FinderUtil.DISCORD_ID.matcher(guildId).matches();
        if (!isValidId) {
            LOGGER.debug("Guild with id {} is not a valid.", guildId);
            return;
        }
        repository.save(new VerifiedServerEntity(guildId));
        LOGGER.debug("Guild Verification with id {} was successful.", guildId);
    }

    public void unverifyServer(String guildId, JDA jda) {
        LOGGER.debug("Unverifying server with id {}", guildId);
        if (jda.getShardManager() == null) {
            sendError("Shardmanager is null. Tried to unverify Server").build();
            return;
        }
        Guild target = getGuildById(jda.getShardManager(), guildId);
        if (target == null) {
            LOGGER.debug("No guild found for guildId {}", guildId);
            return;
        }
        repository.delete(new VerifiedServerEntity(guildId));
        target.leave().queue();
        LOGGER.debug("Successfully left server {} with ID {}", target.getName(), target.getId());
    }

    @Override
    public void execute(CommandContext ctx) {
        String option = ctx.getArgs().split(" ")[0].toLowerCase();
        String guildId = ctx.getArgs().split(" ")[1];
        leaveGuildIfNotVerified(ctx.getEvent().getGuild());
        switch (option) {
            case "add":
                verifyServer(guildId);
                ctx.getEvent().getChannel().sendMessageEmbeds(
                        sendSuccess("Added " + guildId + " to list of verified servers").build()).queue();
                break;
            case "remove":
            case "delete":
                unverifyServer(guildId, ctx.getEvent().getJDA());
                ctx.getEvent().getChannel().sendMessageEmbeds(
                        sendSuccess("Removed " + guildId + " from list of verified servers").build()).queue();
        }
    }

    @Override
    public void handleSlashCommand(SlashCommandEvent event) {
        boolean verify = Boolean.parseBoolean(event.getOptions().get(0).getAsString());
        String guildId = event.getOptions().get(1).getAsString();
        if (verify) {
            verifyServer(guildId);
            event.replyEmbeds(sendSuccess("Added " + guildId + " to list of verified servers").build())
                    .setEphemeral(true)
                    .queue();
        } else {
            unverifyServer(guildId, event.getJDA());
            event.replyEmbeds(sendSuccess("Removed " + guildId + " from list of verified servers").build())
                    .setEphemeral(true)
                    .queue();
        }
    }

    @Override
    public CommandData getSlashCommand() {
        return new CommandData(getName().toLowerCase(), getDescription())
                .addOption(OptionType.BOOLEAN, OPTION_VERIFY, "If true then verify otherwise unverify", true)
                .addOption(OptionType.STRING, OPTION_GUILD_ID, "The server to verify", true);
    }

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public boolean isGuildSlashCommand() {
        return true;
    }

    @Override
    public boolean validate(SlashCommandEvent event) {
        String guildId = event.getOptions().get(1).getAsString();
        return FinderUtil.DISCORD_ID.matcher(guildId).matches();
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
        String guildId = ctx.getArgs().split(" ")[1];
        boolean validOption = option.equals("add") || option.equals("delete") || option.equals("remove");

        if (!FinderUtil.DISCORD_ID.matcher(guildId).matches() || !validOption) {
            ctx.getEvent().getChannel().sendMessageEmbeds(getUsage()).queue();
            return false;
        }

        return true;
    }

    @Override
    public String getDescription() {
        return "If a server is flagged as verified then bot will not automatically leave that server.";
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
        String usage = "```css\n." + COMMAND_NAME + " <add/delete> <server_id>\n```";
        return Collections.singletonList(
                getUsageEmbed(this, emoteConfig)
                        .addField("Usage", usage, false).build()
        );
    }

    @Override
    public Set<String> getAliases() {
        return Set.of("vs");
    }

    @Override
    public long getCooldown() {
        return 0;
    }
}
