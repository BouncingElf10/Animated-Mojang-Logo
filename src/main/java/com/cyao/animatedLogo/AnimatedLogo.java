package com.cyao.animatedLogo;

import net.fabricmc.api.ModInitializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnimatedLogo implements ModInitializer {
    public static final String MOD_ID = "assets/animated-logo";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Identifier STARTUP_SOUND_ID = Identifier.of("animated-logo", "startup");

    public static final SoundEvent STARTUP_SOUND_EVENT = SoundEvent.of(STARTUP_SOUND_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Startup Animation");
        Registry.register(Registries.SOUND_EVENT, STARTUP_SOUND_ID, STARTUP_SOUND_EVENT);
        if (STARTUP_SOUND_ID != null) {
            LOGGER.info("Startup sound loaded successfully!");
        } else {
            LOGGER.warn("Startup sound failed to load!");
        }
    }
}
