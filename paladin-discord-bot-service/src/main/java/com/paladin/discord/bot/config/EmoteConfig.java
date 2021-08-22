package com.paladin.discord.bot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:emote.properties")
public class EmoteConfig {

    private final String enabled;
    private final String disabled;
    private final String paladin;

    public EmoteConfig(@Value("${enabled}") String enabled,
                       @Value("${disabled}") String disabled,
                       @Value("${paladin}") String paladin) {
        this.enabled = enabled;
        this.disabled = disabled;
        this.paladin = paladin;
    }

    public String getEnabled() {
        return enabled;
    }

    public String getDisabled() {
        return disabled;
    }

    public String getPaladin() {
        return paladin;
    }

    public String getBooleanEmote(boolean condition) {
        return condition ? enabled : disabled;
    }
}