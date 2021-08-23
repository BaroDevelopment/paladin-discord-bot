package com.paladin.discord.bot.component.commands.admin;

import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.paladin.discord.bot.component.CommandManager;
import com.paladin.discord.bot.component.commands.ACommand;
import com.paladin.discord.bot.component.commands.CommandContext;
import com.paladin.discord.bot.component.commands.ICommand;
import com.paladin.discord.bot.config.EmoteConfig;
import com.paladin.discord.bot.entities.stickyroles.StickyRoleEntity;
import com.paladin.discord.bot.entities.stickyroles.StickyRoleEntityId;
import com.paladin.discord.bot.entities.stickyroles.StickyRoleLogEntity;
import com.paladin.discord.bot.entities.stickyroles.StickyRoleLogEntityId;
import com.paladin.discord.bot.enums.CommandCategory;
import com.paladin.discord.bot.repository.database.StickRoleLogRepository;
import com.paladin.discord.bot.repository.database.StickRoleRepository;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Component
public class StickyRoleCommand extends ACommand implements ICommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(StickyRoleCommand.class);
    private static final String COMMAND_NAME = "sticky";
    private final EmoteConfig emoteConfig;
    private final StickRoleRepository repository;
    private final StickRoleLogRepository logRepository;
    private static final String OPTION_ADD = "add";
    private static final String OPTION_REMOVE = "remove";
    private static final String OPTION_DELETE = "delete";

    public StickyRoleCommand(EmoteConfig emoteConfig, StickRoleRepository repository,
                             StickRoleLogRepository logRepository) {
        this.emoteConfig = emoteConfig;
        this.repository = repository;
        this.logRepository = logRepository;
    }

    public void handleUserLeave(String serverId, Member member, String userId) {

        if (member == null) {
            LOGGER.debug("Null member left - can not log sticky roles.");
            return;
        }

        for (Role role : member.getRoles()) {
            if (isSticky(serverId, role.getId())) {
                LOGGER.debug("Adding user with userId {}, roleId {} and serverId {} to sticky_role_log",
                        userId, role.getId(), serverId);
                logRepository.save(new StickyRoleLogEntity(new StickyRoleLogEntityId(serverId, role.getId(), userId)));
            }
        }
    }

    public void handleUserJoin(String serverId, String userId, Guild guild) {
        List<StickyRoleLogEntityId> entities = logRepository.findRolesOfLeftUser(serverId, userId);
        for (StickyRoleLogEntityId entity : entities) {
            guild.retrieveMemberById(entity.getUserId()).queue(member -> {
                Role role = guild.getRoleById(entity.getRoleId());
                if (role != null) {
                    guild.addRoleToMember(entity.getUserId(), role).queue();
                    LOGGER.debug("Successfully added sticky role {} to {}", role.getName(), member.getUser().getName());
                    logRepository.delete(new StickyRoleLogEntity(entity));
                } else {
                    LOGGER.debug("Failed to add role to user {}", member.getUser().getName());
                }
            });
        }
    }

    public boolean isSticky(String serverId, String roleId) {
        LOGGER.debug("Checking if role with ID {} is sticky in server with ID {}", roleId, serverId);
        return repository.findById(new StickyRoleEntityId(serverId, roleId)).isPresent();
    }

    public void addSticky(String guildId, String roleId) {
        LOGGER.debug("Adding roleId {} in server with ID {} as sticky", roleId, guildId);
        StickyRoleEntityId id = new StickyRoleEntityId(guildId, roleId);
        repository.save(new StickyRoleEntity(id));
    }

    public void deleteSticky(String guildId, String roleId) {
        LOGGER.debug("Removing roleId {} in server with ID {} from list of sticky roles", roleId, guildId);
        StickyRoleEntityId id = new StickyRoleEntityId(guildId, roleId);
        repository.delete(new StickyRoleEntity(id));
    }

    @Override
    public void execute(CommandContext ctx) {
        for (Role role : ctx.getEvent().getGuild().getRoles()) {
            LOGGER.info("Name: {}, ID: {}", role.getName(), role.getId());
        }
        String option = ctx.getArgs().split(" ")[0].toLowerCase();
        String roleId = ctx.getArgs().split(" ")[1];
        Role target = ctx.getEvent().getGuild().getRoleById(roleId);
        assert target != null;
        switch (option) {
            case OPTION_ADD:
                addSticky(ctx.getEvent().getGuild().getId(), roleId);
                ctx.getEvent().getChannel().sendMessageEmbeds(
                        sendSuccess("Role " + target.getAsMention() + " is now sticky.").build()).queue();
                break;
            case OPTION_REMOVE:
            case OPTION_DELETE:
                deleteSticky(ctx.getEvent().getGuild().getId(), roleId);
                ctx.getEvent().getChannel().sendMessageEmbeds(
                        sendSuccess("Removed " + target.getAsMention() + " from list of sticky roles.").build()).queue();
                break;
            default:
                LOGGER.debug("Invalid option: {} provided", option);
        }
    }

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    @PostConstruct
    public void init() {
        CommandManager.COMMANDS.put(COMMAND_NAME, this);
    }

    @Override
    public boolean validate(CommandContext ctx) {
        if (ctx.getArgs().split(" ").length != 2) {
            ctx.getEvent().getChannel().sendMessageEmbeds(getUsage()).queue();
            return false;
        }
        // check if argument
        String option = ctx.getArgs().split(" ")[0];
        String roleId = ctx.getArgs().split(" ")[1];
        boolean validOption = option.equals(OPTION_ADD) ||
                option.equals(OPTION_DELETE) ||
                option.equals(OPTION_REMOVE);

        if (!FinderUtil.DISCORD_ID.matcher(roleId).matches() || !validOption) {
            ctx.getEvent().getChannel().sendMessageEmbeds(getUsage()).queue();
            return false;
        }

        if (ctx.getEvent().getGuild().getRoleById(roleId) == null) {
            ctx.getEvent().getChannel().sendMessageEmbeds(sendError("Role not found").build()).queue();
            return false;
        }

        return true;
    }

    @Override
    public String getDescription() {
        return "Add or remove a sticky role";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.ADMIN;
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
        String usage = "```css\n." + COMMAND_NAME + " " + format + "<role_id>\n```";
        return Collections.singletonList(
                getUsageEmbed(this, emoteConfig)
                        .addField("Usage", usage, false).build()
        );
    }

    @Override
    public Set<String> getAliases() {
        return Set.of("sr");
    }

    @Override
    public long getCooldown() {
        return 0;
    }

    @Override
    public EnumSet<Permission> getMemberPermissions() {
        return EnumSet.of(Permission.ADMINISTRATOR);
    }

    @Override
    public EnumSet<Permission> getBotPermissions() {
        return EnumSet.of(Permission.ADMINISTRATOR);
    }
}
