package com.bouncingelf10.animatedLogo;

import net.fabricmc.api.ModInitializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.bouncingelf10.animatedLogo.DarkLoadingScreenCompat.isDarkLoadingScreenNotPresent;

public class AnimatedLogo implements ModInitializer {
    public static final String MOD_ID = "animated-mojang-logo";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Identifier STARTUP_SOUND_ID = Identifier.of("animated-mojang-logo", "startup");
    public static final SoundEvent STARTUP_SOUND_EVENT = SoundEvent.of(STARTUP_SOUND_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Startup Animation, DLS: {}", !isDarkLoadingScreenNotPresent());
        Registry.register(Registries.SOUND_EVENT, STARTUP_SOUND_ID, STARTUP_SOUND_EVENT);
    }
}
