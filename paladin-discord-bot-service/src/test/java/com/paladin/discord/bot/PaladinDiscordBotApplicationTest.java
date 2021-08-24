package com.paladin.discord.bot;

public class PaladinDiscordBotApplicationTest {

    public static void main(String[] args) {
        var application = PaladinDiscordBotApplication.createSpringApplication();

        // Here we add the same initializer as we were using in our tests...
        application.addInitializers(new PaladinDockerTestContainers.Initializer());

        // ... and start it normally
        application.run(args);
    }
}
