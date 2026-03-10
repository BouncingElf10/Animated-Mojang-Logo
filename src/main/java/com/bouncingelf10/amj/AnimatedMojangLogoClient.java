package com.bouncingelf10.amj;

import com.bouncingelf10.amj.config.ModConfig;
import com.bouncingelf10.amj.sound.ModSounds;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnimatedMojangLogoClient implements ClientModInitializer {
	public static final String MOD_ID = "animated-mojang-logo";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static boolean isInit = false;
	public static boolean hasRunOnce = false;

	@Override
	public void onInitializeClient() {
		LOGGER.info("Initializing Animated Mojang Logo");
		isInit = true;
		ModSounds.initialize();
		ModConfig.load();
	}
}