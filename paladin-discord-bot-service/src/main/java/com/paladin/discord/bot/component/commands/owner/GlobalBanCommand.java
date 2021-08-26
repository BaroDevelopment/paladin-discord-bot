package com.paladin.discord.bot.component.commands.owner;

import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.paladin.discord.bot.component.CommandManager;
import com.paladin.discord.bot.component.commands.ACommand;
import com.paladin.discord.bot.component.commands.CommandContext;
import com.paladin.discord.bot.component.commands.ICommand;
import com.paladin.discord.bot.component.commands.ISlashCommand;
import com.paladin.discord.bot.config.EmoteConfig;
import com.paladin.discord.bot.entities.GlobalBanEntity;
import com.paladin.discord.bot.enums.CommandCategory;
import com.paladin.discord.bot.repository.GlobalBanRepository;
import com.paladin.discord.bot.util.ColorUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
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
public class GlobalBanCommand extends ACommand implements ICommand, ISlashCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalBanCommand.class);
    private static final String COMMAND_NAME = "globalban";
    private final EmoteConfig emoteConfig;
    private final GlobalBanRepository repository;

    public GlobalBanCommand(EmoteConfig emoteConfig, GlobalBanRepository repository) {
        this.emoteConfig = emoteConfig;
        this.repository = repository;
    }

    void applyGlobalBan(JDA jda, String userId, MessageChannel channel) {

        if (jda == null || jda.getShardManager() == null) {
            LOGGER.error("Failed to apply global ban. ShardManager is null!");
            return;
        }
        repository.save(new GlobalBanEntity(userId));
        for (Guild guild : getGuilds(jda.getShardManager())) {
            globalBan(userId, guild, channel);
        }
    }

    public boolean handleGlobalBanOnGuildJoin(String userId, Guild guild) {
        Optional<GlobalBanEntity> entity = repository.findById(userId);
        if (entity.isPresent()) {
            LOGGER.info("Gloabally banned user with id {} tried to join guild {} and got banned.", userId,
                    guild.getName());
            globalBan(userId, guild);
            return true;
        }
        return false;
    }

    private void globalBan(String userId, Guild guild) {
        EmbedBuilder eb = new EmbedBuilder().setColor(ColorUtil.getRandomHsbColor());
        guild.retrieveMemberById(userId).queue(
                member -> {
                    LOGGER.debug("Applying global ban to user {} with id {}", member.getUser().getName(), userId);
                    guild.ban(member.getUser(), 0).queue(unused -> eb
                            .setTitle("Global Ban")
                            .setDescription("Applied global ban on a user")
                            .addField("Username", member.getUser().getName(), false)
                            .addField("User ID", member.getId(), false)
                            .setThumbnail(member.getUser().getEffectiveAvatarUrl()));
                },
                throwable -> {
                });
    }

    private void globalBan(String userId, Guild guild, MessageChannel channel) {
        EmbedBuilder eb = new EmbedBuilder().setColor(ColorUtil.getRandomHsbColor());
        guild.retrieveMemberById(userId).queue(
                member -> {
                    LOGGER.debug("Global banning user {} with id {} from guild {}", member.getUser().getName(), userId,
                            guild.getName());
                    guild.ban(member.getUser(), 0).queue(unused -> {
                        eb
                                .setTitle("Global Ban")
                                .setDescription("Applied global ban on a user")
                                .addField("Username", member.getUser().getName(), false)
                                .addField("User ID", member.getId(), false)
                                .addField("Server", guild.getName(), false)
                                .addField("Server ID", guild.getId(), false)
                                .setThumbnail(guild.getIconUrl())
                                .setImage(member.getUser().getEffectiveAvatarUrl());

                        member.getUser().openPrivateChannel().queue(privateChannel -> {
                            privateChannel.sendMessageEmbeds(eb.build()).queue();
                            eb.addField("Notified", "YES", true);
                            channel.sendMessageEmbeds(eb.build()).queue();
                        }, throwable -> {
                            eb.addField("Notified", "NO", true);
                            channel.sendMessageEmbeds(eb.build()).queue();
                        });
                    });
                },
                throwable -> {
                });
    }

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public CommandData getSlashCommand() {
        return new CommandData(getName().toLowerCase(), getDescription())
                .addOption(OptionType.STRING, "user_id", "Id of user to global ban", true);
    }

    @Override
    public void handleSlashCommand(SlashCommandEvent event) {
        String userId = event.getOptions().get(0).getAsString();
        applyGlobalBan(event.getJDA(), userId, event.getChannel());
    }

    @Override
    public boolean isGuildSlashCommand() {
        return true;
    }

    @Override
    public boolean validate(SlashCommandEvent event) {
        String userId = event.getOptions().get(0).getAsString();
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
        if (ctx.getArgs().split(" ").length != 1) {
            ctx.getEvent().getChannel().sendMessageEmbeds(getUsage()).queue();
            return false;
        }

        if (!FinderUtil.DISCORD_ID.matcher(ctx.getArgs()).matches()) {
            ctx.getEvent().getChannel().sendMessageEmbeds(getUsage()).queue();
            return false;
        }
        return true;
    }

    @Override
    public void execute(CommandContext ctx) {
        String userId = ctx.getArgs();
        applyGlobalBan(ctx.getEvent().getJDA(), userId, ctx.getEvent().getChannel());
    }

    @Override
    public String getDescription() {
        return "Ban a user from all servers";
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
        String usage = "```css\n." + COMMAND_NAME + " <user_id>\n```";
        return Collections.singletonList(
                getUsageEmbed(this, emoteConfig)
                        .addField("Usage", usage, false).build()
        );
    }

    @Override
    public Set<String> getAliases() {
        return Set.of("gb");
    }

    @Override
    public long getCooldown() {
        return 0;
    }
}
