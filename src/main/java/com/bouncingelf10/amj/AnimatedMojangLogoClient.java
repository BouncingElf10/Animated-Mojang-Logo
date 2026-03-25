package com.bouncingelf10.amj;

import com.bouncingelf10.amj.config.ModConfig;
import com.bouncingelf10.amj.config.ModConfigScreen;
import com.bouncingelf10.amj.sound.ModSounds;
import com.bouncingelf10.amj.timelesslib.animation.AnimationManager;
import com.bouncingelf10.amj.timelesslib.countdown.ClientCountdownManager;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(value = AnimatedMojangLogoClient.MOD_ID, dist = Dist.CLIENT)
public class AnimatedMojangLogoClient {
    public static final String MOD_ID = "animatedmojanglogo";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static boolean isInit = false;
    public static boolean hasRunOnce = false;

    public static final ClientCountdownManager<Minecraft> clientCountdownManager = new ClientCountdownManager<>(Minecraft::getInstance);
    public static final AnimationManager clientAnimationManager = new AnimationManager();

    public AnimatedMojangLogoClient(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing Animated Mojang Logo");

        isInit = true;

        ModSounds.register(modEventBus);
        ModConfig.load();

        modContainer.registerExtensionPoint(
                IConfigScreenFactory.class,
                (mc, parent) -> ModConfigScreen.create(parent)
        );
    }

    public static ClientCountdownManager<Minecraft> getClientCountdownManager() {
        return clientCountdownManager;
    }
    public static AnimationManager getClientAnimationManager() {
        return clientAnimationManager;
    }
}