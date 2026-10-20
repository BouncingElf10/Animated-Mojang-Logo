package com.bouncingelf10.amj.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.neoforged.neoforge.client.loading.NeoForgeLoadingOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NeoForgeLoadingOverlay.class)
public interface NeoForgeLoadingOverlayAccessor {
    @Accessor long getFadeOutStart();

    @Accessor float getCurrentProgress();
    @Accessor void setCurrentProgress(float value);

    @Accessor ReloadInstance getReload();
    @Accessor Minecraft getMinecraft();
}
