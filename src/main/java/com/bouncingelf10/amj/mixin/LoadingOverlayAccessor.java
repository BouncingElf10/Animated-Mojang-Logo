package com.bouncingelf10.amj.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(net.minecraft.client.gui.screens.LoadingOverlay.class)
public interface LoadingOverlayAccessor {
    @Accessor("minecraft")
    Minecraft getMinecraft();

    @Accessor("reload")
    ReloadInstance getReload();

    @Accessor("fadeIn")
    boolean getFadeIn();

    @Accessor("currentProgress")
    float getCurrentProgress();

    @Accessor("currentProgress")
    void setCurrentProgress(float value);

    @Accessor("fadeOutStart")
    long getFadeOutStart();

    @Accessor("fadeInStart")
    long getFadeInStart();

    @Accessor("fadeInStart")
    void setFadeInStart(long value);
}