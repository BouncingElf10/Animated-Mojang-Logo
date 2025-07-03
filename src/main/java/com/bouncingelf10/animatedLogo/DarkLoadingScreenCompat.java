package com.bouncingelf10.animatedLogo;

import net.fabricmc.loader.api.FabricLoader;

public class DarkLoadingScreenCompat {

    public static boolean isDarkLoadingScreenPresent() {
        return FabricLoader.getInstance().isModLoaded("dark-loading-screen");
    }

    public static int getBarColor(int fallback) {
        if (!isDarkLoadingScreenPresent()) return fallback;

        try {
            Class<?> configClass = Class.forName("io.github.a5b84.darkloadingscreen.config.Config");
            Object config = configClass.getMethod("read").invoke(null);
            return configClass.getField("bar").getInt(config);
        } catch (Exception e) {
            e.printStackTrace();
            return fallback;
        }
    }

    public static int getBorderColor(int fallback) {
        if (!isDarkLoadingScreenPresent()) return fallback;

        try {
            Class<?> configClass = Class.forName("io.github.a5b84.darkloadingscreen.config.Config");
            Object config = configClass.getMethod("read").invoke(null);
            return configClass.getField("border").getInt(config);
        } catch (Exception e) {
            e.printStackTrace();
            return fallback;
        }
    }

    public static int getLogoColor(int fallback) {
        if (!isDarkLoadingScreenPresent()) return fallback;

        try {
            Class<?> configClass = Class.forName("io.github.a5b84.darkloadingscreen.config.Config");
            Object config = configClass.getMethod("read").invoke(null);
            return configClass.getField("logo").getInt(config);
        } catch (Exception e) {
            e.printStackTrace();
            return fallback;
        }
    }
}
