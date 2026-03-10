package com.bouncingelf10.amj.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.awt.*;

public class ModConfigScreen {

    public static Screen create(Screen parent) {
        ModConfig defaults = ModConfig.INSTANCE.defaults();
        ModConfig config = ModConfig.INSTANCE.instance();

        return YetAnotherConfigLib.createBuilder()
                .title(Component.literal("Animated Mojang Logo"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("General"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Enabled"))
                                .description(OptionDescription.of(Component.literal("Enables or disables the mod")))
                                .binding(defaults.isEnabled, () -> config.isEnabled, val -> config.isEnabled = val)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Override Colors"))
                                .description(OptionDescription.of(Component.literal("Use custom colors instead of defaults.")))
                                .binding(defaults.overrideColors, () -> config.overrideColors, val -> config.overrideColors = val)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Should Only Play Once"))
                                .description(OptionDescription.of(Component.literal("Determines if the logo should only play once on startup or always play even when reloading resources.")))
                                .binding(defaults.onlyPlayOnce, () -> config.onlyPlayOnce, val -> config.onlyPlayOnce = val)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Float>createBuilder()
                                .name(Component.literal("Hold Seconds"))
                                .description(OptionDescription.of(Component.literal("How many seconds the logo should stay on screen before fading to the main menu.")))
                                .binding(defaults.holdSeconds, () -> config.holdSeconds, val -> config.holdSeconds = val)
                                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(0f, 5f).step(0.1f))
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Colors"))
                        .option(Option.<Color>createBuilder()
                                .name(Component.literal("Background"))
                                .binding(defaults.background, () -> config.background, val -> config.background = val)
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Component.literal("Bar"))
                                .binding(defaults.bar, () -> config.bar, val -> config.bar = val)
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Component.literal("Bar Background"))
                                .binding(defaults.barBackground, () -> config.barBackground, val -> config.barBackground = val)
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Component.literal("Bar Border"))
                                .binding(defaults.border, () -> config.border, val -> config.border = val)
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Component.literal("Logo"))
                                .description(OptionDescription.of(Component.literal("The color of the \"MOJANG\" part of the logo")))
                                .binding(defaults.logo, () -> config.logo, val -> config.logo = val)
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Component.literal("Studios"))
                                .description(OptionDescription.of(Component.literal("The color of the \"STUDIOS\" part of the logo")))
                                .binding(defaults.studios, () -> config.studios, val -> config.studios = val)
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .build())
                .save(() -> ModConfig.INSTANCE.save())
                .build()
                .generateScreen(parent);
    }
}