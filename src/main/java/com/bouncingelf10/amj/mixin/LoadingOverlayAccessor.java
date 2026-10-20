package com.bouncingelf10.amj.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LoadingOverlay.class)
public interface LoadingOverlayAccessor {
    @Accessor long getFadeOutStart();

    @Accessor boolean getFadeIn();
    @Accessor long getFadeInStart();
    @Accessor void setFadeInStart(long value);

    @Accessor float getCurrentProgress();
    @Accessor void setCurrentProgress(float value);

    @Accessor ReloadInstance getReload();
    @Accessor Minecraft getMinecraft();
}
