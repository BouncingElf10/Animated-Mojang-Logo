package com.bouncingelf10.amj.config;

import com.bouncingelf10.amj.AnimatedMojangLogoClient;
import com.bouncingelf10.amj.internal.ColorManager;
import dev.isxander.yacl3.config.GsonConfigInstance;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;

public class ModConfig {
    public static ConfigClassHandler<ModConfig> INSTANCE = ConfigClassHandler.createBuilder(ModConfig.class)
            .id(ResourceLocation.fromNamespaceAndPath(AnimatedMojangLogoClient.MOD_ID, "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("animated-mojang-logo.json")).build()).build();

    @SerialEntry public boolean isEnabled = true;
    @SerialEntry public boolean overrideColors = false;
    @SerialEntry public boolean onlyPlayOnce = true;
    @SerialEntry public float holdSeconds = 1.5f;

    @SerialEntry public Color background = ColorManager.hexToColor("14181c");
    @SerialEntry public Color bar = ColorManager.hexToColor("e22837");
    @SerialEntry public Color barBackground = ColorManager.hexToColor("14181c");
    @SerialEntry public Color border = ColorManager.hexToColor("3a3336");
    @SerialEntry public Color logo = ColorManager.hexToColor("ffffff");
    @SerialEntry public Color studios = ColorManager.hexToColor("ffffff");

    public static boolean isEnabled() { return ModConfig.INSTANCE.instance().isEnabled; }
    public static boolean shouldOverrideColors() { return ModConfig.INSTANCE.instance().overrideColors; }
    public static boolean shouldOnlyPlayOnce() { return ModConfig.INSTANCE.instance().onlyPlayOnce; }
    public static Color getBackground() { return ModConfig.INSTANCE.instance().background; }
    public static Color getBar() { return ModConfig.INSTANCE.instance().bar; }
    public static Color getBarBackground() { return ModConfig.INSTANCE.instance().barBackground; }
    public static Color getBorder() { return ModConfig.INSTANCE.instance().border; }
    public static Color getLogo() { return ModConfig.INSTANCE.instance().logo; }
    public static Color getStudios() { return ModConfig.INSTANCE.instance().studios; }
    public static float getHoldSeconds() { return ModConfig.INSTANCE.instance().holdSeconds; }

    public static void load() {
        ModConfig.INSTANCE.load();
    }
}
