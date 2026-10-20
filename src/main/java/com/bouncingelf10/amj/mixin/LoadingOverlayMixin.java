package com.bouncingelf10.amj.mixin;

import com.bouncingelf10.amj.AnimatedMojangLogoClient;
import com.bouncingelf10.amj.config.ModConfig;
import com.bouncingelf10.amj.internal.ColorManager;
import com.bouncingelf10.amj.internal.MojangAnimFrameManager;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.util.Mth;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LoadingOverlay.class)
public class LoadingOverlayMixin {

    @Unique
    private long reloadDoneStart = -1L;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!ModConfig.isEnabled()) return;
        if (AnimatedMojangLogoClient.hasRunOnce && ModConfig.shouldOnlyPlayOnce()) return;

        LoadingOverlayAccessor self = (LoadingOverlayAccessor) this;
        long now = Util.getMillis();

        tickFadeIn(self, now);
        float fadeIn = getFadeInProgress(self, now);

        if (MojangAnimFrameManager.hasFinished && canFinish(self, fadeIn)) return;

        ci.cancel();

        smoothProgress(self);
        MojangAnimFrameManager.tickPreload();

        if (reloadDoneStart == -1L && self.getReload().isDone()) reloadDoneStart = now;
        float fadeOut = getFadeOutProgress(now);

        guiGraphics.fill(0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), ColorManager.getBackground());
        renderProgressBar(guiGraphics, self, fadeOut);

        if (fadeOut >= 2.0F && AnimatedMojangLogoClient.isInit && MojangAnimFrameManager.areFramesPreloaded()) {
            renderMojangAnim(guiGraphics);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void afterRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!MojangAnimFrameManager.hasFinished) return;

        guiGraphics.fill(0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), ColorManager.getBackground());
        MojangAnimFrameManager.render(guiGraphics);
        removeOverlay((LoadingOverlayAccessor) this);
    }

    @Unique
    private void renderMojangAnim(GuiGraphics guiGraphics) {
        if (!MojangAnimFrameManager.hasStarted) {
            MojangAnimFrameManager.start();
        }

        MojangAnimFrameManager.render(guiGraphics);
    }

    @Unique
    private void tickFadeIn(LoadingOverlayAccessor self, long now) {
        if (self.getFadeIn() && self.getFadeInStart() == -1L)
            self.setFadeInStart(now);
    }

    @Unique
    private float getFadeOutProgress(long now) {
        return reloadDoneStart > -1L
                ? (float) (now - reloadDoneStart) / 1000.0F
                : -1.0F;
    }

    @Unique
    private float getFadeInProgress(LoadingOverlayAccessor self, long now) {
        return self.getFadeInStart() > -1L
                ? (float) (now - self.getFadeInStart()) / 500.0F
                : -1.0F;
    }

    @Unique
    private void smoothProgress(LoadingOverlayAccessor self) {
        float raw = self.getReload().getActualProgress();
        self.setCurrentProgress(
                Mth.clamp(self.getCurrentProgress() * 0.95F + raw * 0.05F, 0.0F, 1.0F)
        );
    }

    @Unique
    private void renderProgressBar(GuiGraphics guiGraphics, LoadingOverlayAccessor self, float fadeOut) {
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
    private void removeOverlay(LoadingOverlayAccessor self) {
        self.getMinecraft().setOverlay(null);
    }

    @Unique
    private boolean canFinish(LoadingOverlayAccessor self, float fadeIn) {
        return self.getFadeOutStart() == -1L && self.getReload().isDone() && (!self.getFadeIn() || fadeIn >= 2.0F);
    }
}
