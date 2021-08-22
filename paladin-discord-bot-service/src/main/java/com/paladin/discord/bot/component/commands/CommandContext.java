package com.paladin.discord.bot.component.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class CommandContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandContext.class);
    private final String prefix;
    private final String invoke;
    private final String[] args;
    private final MessageReceivedEvent messageReceivedEvent;

    public CommandContext(String prefix, String invoke, String[] args, MessageReceivedEvent messageReceivedEvent) {
        this.prefix = prefix;
        this.invoke = invoke;
        this.args = args;
        this.messageReceivedEvent = messageReceivedEvent;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getInvoke() {
        return invoke;
    }

    public @NotNull String getArgs() {
        String[] subArray = Arrays.copyOfRange(args, 1, args.length);
        StringBuilder builder = new StringBuilder();
        for (String value : subArray) {
            builder.append(value).append(" ");
        }
        return builder.toString().trim();
    }

    public MessageReceivedEvent getEvent() {
        return messageReceivedEvent;
    }
}
