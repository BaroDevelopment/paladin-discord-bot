package com.paladin.discord.bot.component.commands.misc;

import com.paladin.discord.bot.component.CommandManager;
import com.paladin.discord.bot.component.commands.ACommand;
import com.paladin.discord.bot.component.commands.CommandContext;
import com.paladin.discord.bot.component.commands.ICommand;
import com.paladin.discord.bot.component.commands.ISlashCommand;
import com.paladin.discord.bot.config.BotConfig;
import com.paladin.discord.bot.config.EmoteConfig;
import com.paladin.discord.bot.enums.CommandCategory;
import com.paladin.discord.bot.model.redis.AfkRedisModel;
import com.paladin.discord.bot.repository.redis.RedisStringRepository;
import com.paladin.discord.bot.util.ColorUtil;
import com.paladin.discord.bot.util.FormatUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AfkCommand extends ACommand implements ICommand, ISlashCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(AfkCommand.class);
    private static final String COMMAND_NAME = "afk";
    private final EmoteConfig emoteConfig;
    private final BotConfig botConfig;
    private final RedisStringRepository redisStringRepository;

    public AfkCommand(EmoteConfig emoteConfig, BotConfig botConfig, RedisStringRepository redisStringRepository) {
        this.emoteConfig = emoteConfig;
        this.botConfig = botConfig;
        this.redisStringRepository = redisStringRepository;
    }

    public void handleAfkStateOfUser(MessageReceivedEvent event) {
        handleAfkRemove(event);
        handleAfkUserMentioned(event);
    }

    /**
     * Check if user got afk entry in redis and remove it
     *
     * @param event the MessageReceivedEvent
     */
    private void handleAfkRemove(MessageReceivedEvent event) {
        try {
            String userId = event.getAuthor().getId();
            AfkRedisModel redisResult = redisStringRepository.findOne("afk:" + userId).block();
            if (redisResult != null) {
                OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
                OffsetDateTime past = OffsetDateTime.parse(redisResult.getCreationTime());
                long minutes = ChronoUnit.MINUTES.between(past.toLocalDateTime(), now.toLocalDateTime());
                String afkTime = minutes + (minutes > 1 ? " minutes" : "minute");
                event.getChannel().sendMessageEmbeds(
                        new EmbedBuilder().setColor(ColorUtil.getRandomHsbColor())
                                .setDescription("You afk status has been removed. You have been afk for " + afkTime)
                                .build()
                ).queue();
            }
            redisStringRepository.delete("afk:" + userId).subscribe();
        } catch (Exception e) {
            LOGGER.error("Failed to find user in Redis", e);
        }
    }

    /**
     * Send an embed with all mentioned afk users and afk messages
     *
     * @param event the MessageReceivedEvent
     */
    private void handleAfkUserMentioned(MessageReceivedEvent event) {
        List<User> users = event.getMessage().getMentionedUsers();
        Map<String, AfkRedisModel> afkMap = new HashMap<>();

        users.forEach(user -> {
            try {
                AfkRedisModel redisResult = redisStringRepository.findOne("afk:" + user.getId()).block();
                if (redisResult != null) {
                    afkMap.put(user.getName(), redisResult);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to find user in Redis", e);
            }
        });

        if (afkMap.isEmpty()) return;

        EmbedBuilder eb = new EmbedBuilder().setColor(ColorUtil.getRandomHsbColor())
                .setDescription("You mentioned afk users")
                .setFooter("Times are in GMT+0");
        afkMap.forEach((s, model) -> eb.addField(s + " - afk since " + getTimeAsString(model), model.getMessage(),
                false));
        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    }

    public String getTimeAsString(AfkRedisModel model) {
        return FormatUtil.getDateAndTimestamps(OffsetDateTime.parse(model.getCreationTime()));
    }

    private EmbedBuilder getSuccessMessage(String afkMessage) {
        String result = afkMessage.isEmpty() ? botConfig.getDefaultAfkMessage() : afkMessage;
        return sendSuccess("Your afk message is:\n" + result);
    }

    @Override
    public void execute(CommandContext ctx) {
        String afkMessage = ctx.getArgs().isEmpty() ? botConfig.getDefaultAfkMessage() : ctx.getArgs();
        LOGGER.debug("User {} went afk with message: {}", ctx.getEvent().getAuthor().getAsTag(), afkMessage);
        ctx.getEvent().getChannel().sendMessageEmbeds(
                getSuccessMessage(ctx.getArgs()).build()
        ).queue();
        AfkRedisModel model = new AfkRedisModel(ctx.getEvent().getAuthor().getId(), afkMessage,
                ctx.getEvent().getMessage().getTimeCreated().toString());
        redisStringRepository.save(COMMAND_NAME + ":" + ctx.getEvent().getAuthor().getId(), model,
                botConfig.getRedisCacheDurationMinutes());
    }

    @Override
    public void handleSlashCommand(SlashCommandEvent event) {
        String optionString = event.getOptions().stream()
                .map(OptionMapping::getAsString)
                .collect(Collectors.joining());
        String afkMessage = optionString.isEmpty() ? botConfig.getDefaultAfkMessage() : optionString;
        LOGGER.debug("User {} went afk with message: {}", event.getUser().getAsTag(), afkMessage);
        event.replyEmbeds(getSuccessMessage(afkMessage).build()).queue();
        AfkRedisModel model = new AfkRedisModel(event.getUser().getId(), afkMessage, event.getTimeCreated().toString());
        redisStringRepository.save(COMMAND_NAME + ":" + event.getUser().getId(), model,
                botConfig.getRedisCacheDurationMinutes());
    }

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public CommandData getSlashCommand() {
        return new CommandData(getName().toLowerCase(), getDescription())
                .addOption(OptionType.STRING, "message", "Your afk message");
    }

    @Override
    public boolean validate(SlashCommandEvent event) {
        return true;
    }

    @Override
    public boolean validate(CommandContext ctx) {
        return true;
    }

    @Override
    public String getDescription() {
        return "Set AFK status";
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
        String usage = "```css\n." + COMMAND_NAME + " <afk_message>\n```";
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
    @PostConstruct
    public void init() {
        CommandManager.COMMANDS.put(COMMAND_NAME, this);
        CommandManager.SLASH_COMMANDS.put(COMMAND_NAME, this);
    }
}
