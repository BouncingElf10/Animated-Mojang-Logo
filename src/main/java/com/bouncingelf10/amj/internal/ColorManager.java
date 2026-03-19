package com.bouncingelf10.amj.internal;

import com.bouncingelf10.amj.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ARGB;
import org.joml.Vector3f;

import java.awt.*;
import java.util.function.IntSupplier;

public class ColorManager {
    private static final int LOGO_BACKGROUND_COLOR = ARGB.color(255, 239, 50, 61);
    private static final int LOGO_BACKGROUND_COLOR_DARK = ARGB.color(255, 0, 0, 0);
    private static final IntSupplier BRAND_BACKGROUND = () -> (Boolean) Minecraft.getInstance().options.darkMojangStudiosBackground().get() ? LOGO_BACKGROUND_COLOR_DARK : LOGO_BACKGROUND_COLOR;
    private static final IntSupplier BRAND_TEXT = () -> 0xffffff;

    public static int getBackground() {
        Color colorString = ModConfig.getBackground();
        return returnColorOrBase(getColor(colorString));
    }
    public static int getBar() {
        Color colorString = ModConfig.getBar();
        return returnColorOrBaseText(getColor(colorString));
    }
    public static int getBarBackground() {
        Color colorString = ModConfig.getBarBackground();
        return returnColorOrBase(getColor(colorString));
    }
    public static int getBorder() {
        Color colorString = ModConfig.getBorder();
        return returnColorOrBaseText(getColor(colorString));
    }
    public static int getLogo() {
        Color colorString = ModConfig.getLogo();
        return returnColorOrBaseText(getColor(colorString));
    }
    public static int getStudios() {
        Color colorString = ModConfig.getStudios();
        return returnColorOrBaseText(getColor(colorString));
    }

    public static int returnColorOrBase(int color) {
        if (ModConfig.shouldOverrideColors()) return color;
        return BRAND_BACKGROUND.getAsInt();
    }

    public static int returnColorOrBaseText(int color) {
        if (ModConfig.shouldOverrideColors()) return color;
        return BRAND_TEXT.getAsInt();
    }

    public static int getColor(String color) {
        if (color.startsWith("#")) { color = color.substring(1); }

        int rgb = Integer.parseInt(color, 16);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        return ARGB.color(255, r, g, b);
    }

    public static int getColor(Color color) {
        return ARGB.color(255, color.getRed(), color.getGreen(), color.getBlue());
    }

    public static int applyAlpha(int color, float alpha) {
        int a = Math.round(alpha * 255.0F);
        int r = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int b = color & 255;
        return ARGB.color(a, r, g, b);
    }

    public static Vector3f getColorVec(int color) {
        return new Vector3f((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F);
    }

    public static Color hexToColor(String hex) {
        Vector3f color = getColorVec(getColor(hex));
        return new Color(color.x, color.y, color.z);
    }
}
