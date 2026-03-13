package com.bouncingelf10.amj.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.awt.*;

public class ModConfigScreen {

    public static Screen create(Screen parent) {
        ModConfig defaults = ModConfig.INSTANCE.defaults();
        ModConfig config = ModConfig.INSTANCE.instance();

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("config.animated-mojang-logo.title"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("config.animated-mojang-logo.category.general"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("config.animated-mojang-logo.enabled"))
                                .description(OptionDescription.of(Component.translatable("config.animated-mojang-logo.enabled.desc")))
                                .binding(defaults.isEnabled, () -> config.isEnabled, val -> config.isEnabled = val)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("config.animated-mojang-logo.override_colors"))
                                .description(OptionDescription.of(Component.translatable("config.animated-mojang-logo.override_colors.desc")))
                                .binding(defaults.overrideColors, () -> config.overrideColors, val -> config.overrideColors = val)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("config.animated-mojang-logo.only_play_once"))
                                .description(OptionDescription.of(Component.translatable("config.animated-mojang-logo.only_play_once.desc")))
                                .binding(defaults.onlyPlayOnce, () -> config.onlyPlayOnce, val -> config.onlyPlayOnce = val)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Float>createBuilder()
                                .name(Component.translatable("config.animated-mojang-logo.hold_seconds"))
                                .description(OptionDescription.of(Component.translatable("config.animated-mojang-logo.hold_seconds.desc")))
                                .binding(defaults.holdSeconds, () -> config.holdSeconds, val -> config.holdSeconds = val)
                                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(0f, 5f).step(0.1f))
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("config.animated-mojang-logo.category.colors"))
                        .option(Option.<Color>createBuilder()
                                .name(Component.translatable("config.animated-mojang-logo.background"))
                                .binding(defaults.background, () -> config.background, val -> config.background = val)
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Component.translatable("config.animated-mojang-logo.bar"))
                                .binding(defaults.bar, () -> config.bar, val -> config.bar = val)
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Component.translatable("config.animated-mojang-logo.bar_background"))
                                .binding(defaults.barBackground, () -> config.barBackground, val -> config.barBackground = val)
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Component.translatable("config.animated-mojang-logo.bar_border"))
                                .binding(defaults.border, () -> config.border, val -> config.border = val)
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Component.translatable("config.animated-mojang-logo.logo"))
                                .description(OptionDescription.of(Component.translatable("config.animated-mojang-logo.logo.desc")))
                                .binding(defaults.logo, () -> config.logo, val -> config.logo = val)
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Component.translatable("config.animated-mojang-logo.studios"))
                                .description(OptionDescription.of(Component.translatable("config.animated-mojang-logo.studios.desc")))
                                .binding(defaults.studios, () -> config.studios, val -> config.studios = val)
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .build())
                .save(() -> ModConfig.INSTANCE.save())
                .build()
                .generateScreen(parent);
    }
}