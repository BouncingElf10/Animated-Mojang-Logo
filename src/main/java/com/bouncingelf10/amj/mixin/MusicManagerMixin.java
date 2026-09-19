package com.bouncingelf10.amj.mixin;

import com.bouncingelf10.amj.AnimatedMojangLogoClient;
import com.bouncingelf10.amj.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.sounds.MusicManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MusicManager.class)
public class MusicManagerMixin {

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void amj$holdDuringAnimation(CallbackInfo ci) {
		if (Minecraft.getInstance().gui.overlay() instanceof LoadingOverlay
				&& ModConfig.isEnabled()
				&& !(AnimatedMojangLogoClient.hasRunOnce && ModConfig.shouldOnlyPlayOnce()))
			ci.cancel();
	}
}
