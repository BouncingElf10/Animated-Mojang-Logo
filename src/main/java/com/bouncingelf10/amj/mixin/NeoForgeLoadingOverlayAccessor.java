package com.bouncingelf10.amj.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(LoadingOverlay.class)
public interface NeoForgeLoadingOverlayAccessor {
    @Accessor long getFadeOutStart();
    @Accessor void setFadeOutStart(long value);

    @Accessor("fadeIn")
    boolean getFadeIn();

    @Accessor("fadeInStart")
    long getFadeInStart();
    @Accessor("fadeInStart")
    void setFadeInStart(long value);

    @Accessor float getCurrentProgress();
    @Accessor void setCurrentProgress(float value);

    @Accessor ReloadInstance getReload();
    @Accessor Minecraft getMinecraft();

    @Accessor Consumer<Optional<Throwable>> getOnFinish();
}
