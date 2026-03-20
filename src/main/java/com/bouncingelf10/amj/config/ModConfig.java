package com.bouncingelf10.amj.config;

import com.bouncingelf10.amj.internal.ColorManager;

import java.awt.*;

public class ModConfig {
    public static boolean isEnabled = true;
    public static boolean overrideColors = false;
    public static boolean onlyPlayOnce = true;
    public static float holdSeconds = 1.5f;

    public static Color background = ColorManager.hexToColor("14181c");
    public static Color bar = ColorManager.hexToColor("e22837");
    public static Color barBackground = ColorManager.hexToColor("14181c");
    public static Color border = ColorManager.hexToColor("3a3336");
    public static Color logo = ColorManager.hexToColor("ffffff");
    public static Color studios = ColorManager.hexToColor("ffffff");

    public static boolean isEnabled() { return isEnabled; }
    public static boolean shouldOverrideColors() { return overrideColors; }
    public static boolean shouldOnlyPlayOnce() { return onlyPlayOnce; }
    public static Color getBackground() { return background; }
    public static Color getBar() { return bar; }
    public static Color getBarBackground() { return barBackground; }
    public static Color getBorder() { return border; }
    public static Color getLogo() { return logo; }
    public static Color getStudios() { return studios; }
    public static float getHoldSeconds() { return holdSeconds; }
}
