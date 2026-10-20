package com.bouncingelf10.amj.mixin;

import com.bouncingelf10.amj.AnimatedMojangLogoClient;
import com.bouncingelf10.amj.config.ModConfig;
import com.bouncingelf10.amj.internal.ColorManager;
import com.bouncingelf10.amj.internal.MojangAnimFrameManager;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.MeshData;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import net.neoforged.neoforge.client.loading.NeoForgeLoadingOverlay;

import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NeoForgeLoadingOverlay.class)
public class NeoForgeLoadingOverlayMixin {

    @Unique
    private long reloadDoneStart = -1L;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!ModConfig.isEnabled()) return;
        if (AnimatedMojangLogoClient.hasRunOnce && ModConfig.shouldOnlyPlayOnce()) return;

        NeoForgeLoadingOverlayAccessor self = (NeoForgeLoadingOverlayAccessor) this;

        if (MojangAnimFrameManager.hasFinished && canFinish(self)) return;

        ci.cancel();

        long now = Util.getMillis();

        smoothProgress(self);
        MojangAnimFrameManager.tickPreload();

        if (reloadDoneStart == -1L && self.getReload().isDone()) reloadDoneStart = now;
        float fadeOut = getFadeOutProgress(now);

        guiGraphics.fill(0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), ColorManager.getBackground());
        renderProgressBar(guiGraphics, self, fadeOut);

        if (fadeOut >= 1.0F && AnimatedMojangLogoClient.isInit && MojangAnimFrameManager.areFramesPreloaded()) {
            renderMojangAnim(guiGraphics);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void afterRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (MojangAnimFrameManager.hasFinished) removeOverlay((NeoForgeLoadingOverlayAccessor) this);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferUploader;drawWithShader(Lcom/mojang/blaze3d/vertex/MeshData;)V"))
    private void skipEarlyWindowDraw(MeshData meshData, Operation<Void> original) {
        if (MojangAnimFrameManager.hasFinished) {
            meshData.close();
            return;
        }
        original.call(meshData);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_clearColor(FFFF)V"))
    private void clearWithBackground(float red, float green, float blue, float alpha, Operation<Void> original) {
        if (MojangAnimFrameManager.hasFinished) {
            Vector3f color = ColorManager.getColorVec(ColorManager.getBackground());
            original.call(color.x, color.y, color.z, alpha);
            return;
        }
        original.call(red, green, blue, alpha);
    }

    @Unique
    private void renderMojangAnim(GuiGraphics guiGraphics) {
        if (!MojangAnimFrameManager.hasStarted) {
            MojangAnimFrameManager.start();
        }

        MojangAnimFrameManager.render(guiGraphics);
    }

    @Unique
    private void smoothProgress(NeoForgeLoadingOverlayAccessor self) {
        float raw = self.getReload().getActualProgress();
        self.setCurrentProgress(
                Mth.clamp(self.getCurrentProgress() * 0.95F + raw * 0.05F, 0.0F, 1.0F)
        );
    }

    @Unique
    private float getFadeOutProgress(long now) {
        return reloadDoneStart > -1L
                ? (float) (now - reloadDoneStart) / 1000.0F
                : -1.0F;
    }

    @Unique
    private void renderProgressBar(GuiGraphics guiGraphics, NeoForgeLoadingOverlayAccessor self, float fadeOut) {
        if (fadeOut >= 1.0F) return;

        int w = guiGraphics.guiWidth();
        int h = guiGraphics.guiHeight();
        int centerX = w / 2;

        int barHalfWidth = (int) (Math.min(w * 0.75, h) * 0.25 * 4.0 * 0.5);
        int barY = (int) (h * 0.8325);

        float alpha = 1.0F - Mth.clamp(fadeOut, 0.0F, 1.0F);

        drawProgressBar(guiGraphics,
                centerX - barHalfWidth, barY - 5,
                centerX + barHalfWidth, barY + 5,
                alpha, self.getCurrentProgress()
        );
    }

    @Unique
    private void drawProgressBar(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, float alpha, float progress) {
        int width = x2 - x1;
        int filled = Math.min(Mth.ceil((width - 2) * progress), width - 4);
        int barColor = ColorManager.applyAlpha(ColorManager.getBar(), alpha);
        int backgroundColor = ColorManager.applyAlpha(ColorManager.getBarBackground(), alpha);
        int borderColor = ColorManager.applyAlpha(ColorManager.getBorder(), alpha);

        guiGraphics.fill(x1 + 1, y1, x2 - 1, y1 + 1, borderColor);
        guiGraphics.fill(x1 + 1, y2 - 1, x2 - 1, y2, borderColor);
        guiGraphics.fill(x1, y1, x1 + 1, y2, borderColor);
        guiGraphics.fill(x2 - 1, y1, x2, y2, borderColor);
        guiGraphics.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, backgroundColor);
        guiGraphics.fill(x1 + 2, y1 + 2, x1 + 2 + filled, y2 - 2, barColor);
    }

    @Unique
    private void removeOverlay(NeoForgeLoadingOverlayAccessor self) {
        self.getMinecraft().setOverlay(null);
    }

    @Unique
    private boolean canFinish(NeoForgeLoadingOverlayAccessor self) {
        return self.getFadeOutStart() == -1L && self.getReload().isDone();
    }
}
