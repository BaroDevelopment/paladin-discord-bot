package com.paladin.discord.bot.util;

import com.paladin.discord.bot.service.PaladinBotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Random;

public abstract class ColorUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaladinBotService.class);

    public static Color getRandomHsbColor() {
        //to get rainbow, pastel colors
        final float hue = new Random().nextFloat();
        final float saturation = 0.9f;//1.0 for brilliant, 0.0 for dull
        final float luminance = 1.0f; //1.0 for brighter, 0.0 for black

        return Color.getHSBColor(hue, saturation, luminance);
    }

    public static Color getRandomRgbColor() {
        int R = (int) (Math.random() * 256);
        int G = (int) (Math.random() * 256);
        int B = (int) (Math.random() * 256);

        return new Color(R, G, B);
    }
}
