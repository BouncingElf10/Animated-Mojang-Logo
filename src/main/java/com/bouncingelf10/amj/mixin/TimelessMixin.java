package com.bouncingelf10.amj.mixin;

import com.bouncingelf10.amj.AnimatedMojangLogoClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class TimelessMixin {
    @Inject(method = "runTick(Z)V", at = @At("TAIL"))
    private void onRunTick(boolean partialTick, CallbackInfo ci) {
        AnimatedMojangLogoClient.getClientAnimationManager().update();
    }
}

